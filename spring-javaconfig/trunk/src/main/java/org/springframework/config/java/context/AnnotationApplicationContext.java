/*
 * Copyright 2002-2007 the original author or authors.
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
 */

package org.springframework.config.java.context;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Annotation-aware application context. This class extends
 * {@link AbstractAnnotationApplicationContext} and adds support for reading an
 * application context definition/metadata directly from a class.
 * 
 * @author Costin Leau
 * @author Rod Johnson
 * 
 * TODO should no longer need the superclass
 */
public class AnnotationApplicationContext extends AbstractAnnotationApplicationContext {

	private String[] basePackages;

	private Resource[] configResources;

	private Class[] configClasses;

	/**
	 * Create a new AnnotationApplicationContext w/o any settings.
	 * 
	 * @see #setClassLoader(ClassLoader)
	 * @see #setConfigClasses(Class[])
	 * @see #setBasePackages(String[])
	 * @see #setConfigResources(Resource[])
	 */
	public AnnotationApplicationContext() {
		super();
	}

	/**
	 * Create a new AnnotationApplicationContext with the given parent. The
	 * instance can be further configured before calling {@link #refresh()}.
	 * 
	 * 
	 * @see #setClassLoader(ClassLoader)
	 * @see #setConfigClasses(Class[])
	 * @see #setBasePackages(String[])
	 * @see #setConfigResources(Resource[])
	 * 
	 * @param parent the parent application context
	 */
	public AnnotationApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Create a new AnnotationApplicationContext from the given locations ({@link #refresh()}
	 * is being called).
	 * 
	 * 
	 * @param basePackages the base packages to scan
	 */
	public AnnotationApplicationContext(String... basePackages) {
		setBasePackages(basePackages);
		refresh();
	}

	/**
	 * Create a new AnnotationApplicationContext from the given classes ({@link #refresh()}
	 * is being called).
	 * 
	 * @param classes
	 */
	public AnnotationApplicationContext(Class... classes) {
		setConfigClasses(classes);
		refresh();
	}

	@Override
	protected String[] getBasePackages() {
		return basePackages;
	}

	@Override
	protected Resource[] getConfigResources() {
		return configResources;
	}

	@Override
	protected Class[] getConfigClasses() {
		return configClasses;
	}

	/**
	 * Set the base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in
	 * Spring 2.5.
	 * 
	 * @param basePackages
	 */
	public void setBasePackages(String... basePackages) {
		this.basePackages = basePackages;
	}

	/**
	 * Indicate the configuration locations.
	 * 
	 * @param resources
	 */
	public void setConfigResources(Resource... resources) {
		this.configResources = resources;
	}

	/**
	 * Indicate the {@link Class}es that hold annotations suitable for
	 * configuring the current application context.
	 * @param classes
	 */
	public void setConfigClasses(Class... classes) {
		this.configClasses = classes;
	}
}
