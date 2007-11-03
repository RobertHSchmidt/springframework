/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.springframework.osgi.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Install Bundles using a FactoryBean.
 * 
 * This allows customers to use Spring to drive bundle management. Bundles
 * states can be modified using the state parameter. Most commonly this is set
 * to "start".
 * 
 * <p/>Pay attention when installing bundles dynamically since classes can be
 * loaded aggressively.
 * 
 * @author Andy Piper
 * @author Costin Leau
 */
public class BundleFactoryBean implements FactoryBean, BundleContextAware, InitializingBean, DisposableBean,
		ResourceLoaderAware {

	private static Log log = LogFactory.getLog(BundleFactoryBean.class);

	// bundle info
	/** Bundle location */
	private String location;

	private Resource resource;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/** Bundle symName */
	private String symbolicName;

	/** Actual bundle */
	private Bundle bundle;

	private BundleContext bundleContext;

	private BundleAction action, destroyAction;

	private int startLevel;

	private ClassLoader classloader;

	/** unused at the moment */
	private boolean pushBundleAsContextClassLoader = false;

	/** wait until the operation invoked completes */
	private boolean waitToComplete = false;

	// FactoryBean methods
	public Class getObjectType() {
		return (bundle != null ? bundle.getClass() : Bundle.class);
	}

	public boolean isSingleton() {
		return true;
	}

	public Object getObject() throws Exception {
		return bundle;
	}

	public void afterPropertiesSet() {
		Assert.notNull(bundleContext, "BundleContext is required");

		// check parameters
		if (symbolicName == null && !StringUtils.hasText(location))
			throw new IllegalArgumentException("Bundle-SymbolicName or location required");

		// try creating a resource
		if (getLocation() != null) {
			resource = resourceLoader.getResource(getLocation());
		}

		// find the bundle first of all
		bundle = findBundle();

		executeAction(action);
	}

	public void destroy() throws Exception {
		executeAction(destroyAction);

		bundle = null;
		classloader = null;
	}

	private void executeAction(BundleAction action) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		try {
			if (pushBundleAsContextClassLoader) {
				Thread.currentThread().setContextClassLoader(classloader);
			}

			if (action != null) {
				bundle = action.execute(bundle);
			}
		}
		finally {
			if (pushBundleAsContextClassLoader) {
				Thread.currentThread().setContextClassLoader(ccl);
			}
		}
	}

	/**
	 * Find a bundle based on the configuration (don't apply any actions for
	 * it).
	 * 
	 * @return a Bundle instance based on the configuration.
	 */
	private Bundle findBundle() {
		// first consider symbolicName
		Bundle bundle = null;

		// try to find the bundle
		if (StringUtils.hasText(symbolicName))
			bundle = OsgiBundleUtils.findBundleBySymbolicName(bundleContext, symbolicName);

		// TODO: keep the start-level or not?
		// updateStartLevel(getStartLevel());

		return bundle;
	}

	/**
	 * Return the given bundle symbolic name.
	 * 
	 * @return bundle symbolic name
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	/**
	 * Return the given location.
	 * 
	 * @return bundle location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Return the {@link Resource} object (if a {@link ResourceLoader} is
	 * available) from the given location (if any).
	 * 
	 * @return {@link Resource} object for the given location
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Set the bundle location (optional operation).
	 * 
	 * @param url bundle location (normally an URL or a Spring Resource)
	 * 
	 */
	public void setLocation(String url) {
		location = url;
	}

	/**
	 * Set the bundle symbolic name (optional operation).
	 * 
	 * @param symbolicName bundle symbolic name
	 */
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public void setBundleContext(BundleContext context) {
		bundleContext = context;
	}

	/**
	 * Action to execute at startup.
	 * 
	 * @param action action to execute at startup
	 */
	public void setAction(BundleAction action) {
		this.action = action;
	}

	/**
	 * Action to execute at shutdown.
	 * 
	 * @param action action to execute at shutdown
	 */
	public void setDestroyAction(BundleAction action) {
		this.destroyAction = action;
	}

	public void setStartLevel(int startLevel) {
		this.startLevel = startLevel;
	}

	/**
	 * Determines whether invocations on the remote service should be performed
	 * in the context of the target bundle's ClassLoader. The default is false.
	 * 
	 * @param pushBundleAsContextClassLoader
	 */
	public void setPushBundleAsContextClassloader(boolean pushBundleAsContextClassLoader) {
		this.pushBundleAsContextClassLoader = pushBundleAsContextClassLoader;
	}

	/**
	 * @deprecated use {@link #setClassLoader(ClassLoader)} instead.
	 * @param classloader
	 */
	public void setClassloader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * @return Returns the bundleContext.
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	// TODO: we don't support start-levels yet
	private void updateStartLevel(int level) {
		if (level == 0 || bundle == null)
			return;
		// Set the start level of the bundle if we are able.
		ServiceReference startref = bundleContext.getServiceReference(StartLevel.class.getName());
		if (startref != null) {
			StartLevel start = (StartLevel) bundleContext.getService(startref);
			if (start != null) {
				start.setBundleStartLevel(bundle, level);
			}
			bundleContext.ungetService(startref);
		}
	}

}
