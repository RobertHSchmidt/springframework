/*
 * Copyright 2002-2008 the original author or authors.
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

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Annotation-aware application context that looks for classes annotated with
 * the Configuration annotation and registers the beans they define.
 * 
 * @deprecated - Favor use of {@link JavaConfigApplicationContext}. This class
 * will be eliminated before the 1.0 release of JavaConfig
 * @author Costin Leau
 * @author Rod Johnson
 * @author Chris Beams
 */
// /CLOVER:OFF
@Deprecated
public class AnnotationApplicationContext extends AbstractRefreshableApplicationContext {

	private String[] basePackages;

	private Class<?>[] configClasses;

	/**
	 * We delegate to Spring 2.5 and above class scanning support.
	 */
	private ClassPathScanningCandidateComponentProvider scanner;

	/**
	 * Create a new AnnotationApplicationContext w/o any settings.
	 * 
	 * @see #setClassLoader(ClassLoader)
	 * @see #setConfigClasses(Class[])
	 * @see #setBasePackages(String[])
	 */
	public AnnotationApplicationContext() {
		this((ApplicationContext) null);
	}

	/**
	 * Create a new AnnotationApplicationContext with the given parent. The
	 * instance can be further configured before calling {@link #refresh()}.
	 * 
	 * @see #setClassLoader(ClassLoader)
	 * @see #setConfigClasses(Class[])
	 * @see #setBasePackages(String[])
	 * 
	 * @param parent the parent application context
	 */
	public AnnotationApplicationContext(ApplicationContext parent) {
		super(parent);
		registerDefaultPostProcessors();
		this.scanner = new ClassPathScanningCandidateComponentProvider(false);
		this.scanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		this.scanner.setResourceLoader(this);
	}

	/**
	 * Create a new AnnotationApplicationContext from the given locations ({@link #refresh()}
	 * is being called).
	 * 
	 * @param basePackages the base packages to scan
	 */
	public AnnotationApplicationContext(String... basePackages) {
		this((ApplicationContext) null);
		setBasePackages(basePackages);
		refresh();
	}

	/**
	 * Create a new AnnotationApplicationContext from the given classes ({@link #refresh()}
	 * is being called).
	 * 
	 * @param classes
	 */
	public AnnotationApplicationContext(Class<?>... classes) {
		this((ApplicationContext) null);
		setConfigClasses(classes);
		refresh();
	}

	/**
	 * Create a new AnnotationApplicationContext with the given parent and
	 * classes.
	 * 
	 * @see #AnnotationApplicationContext(ApplicationContext)
	 * @see #AnnotationApplicationContext(Class...)
	 * 
	 * @param parent the parent application context
	 * @param classes zero or more configuration classes
	 */
	public AnnotationApplicationContext(ApplicationContext parent, Class<?>... classes) {
		this(parent);
		setConfigClasses(classes);
		refresh();
	}

	/**
	 * Create a new AnnotationApplicationContext with the given parent and base
	 * packages.
	 * 
	 * @see #AnnotationApplicationContext(ApplicationContext)
	 * @see #AnnotationApplicationContext(String...)
	 * 
	 * @param parent the parent application context
	 * @param basePackages the base packages to scan
	 */
	public AnnotationApplicationContext(ApplicationContext parent, String... basePackages) {
		this(parent);
		setBasePackages(basePackages);
		refresh();
	}

	protected String[] getBasePackages() {
		return basePackages;
	}

	protected Class<?>[] getConfigClasses() {
		return configClasses;
	}

	/**
	 * Set the base packages for configurations from Strings. These use the same
	 * conventions as the component scanning introduced in Spring 2.5.
	 * 
	 * @param basePackages
	 */
	public void setBasePackages(String... basePackages) {
		this.basePackages = basePackages;
	}

	/**
	 * Indicate the {@link Class}es that hold annotations suitable for
	 * configuring the current application context.
	 * @param classes
	 */
	public void setConfigClasses(Class<?>... classes) {
		this.configClasses = classes;
	}

	/**
	 * Register the default post processors used for parsing Spring classes.
	 * 
	 */
	protected void registerDefaultPostProcessors() {
		addBeanFactoryPostProcessor(new ConfigurationPostProcessor());
	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		Class<?>[] configClasses = getConfigClasses();
		if (configClasses != null && configClasses.length > 0) {
			for (Class<?> cz : configClasses) {
				if (ConfigurationProcessor.isConfigurationClass(cz)) {
					beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
				}
			}
		}
		else {
			// Scan classpath
			for (String location : getBasePackages()) {
				Set<BeanDefinition> beandefs = this.scanner.findCandidateComponents(location);
				for (BeanDefinition bd : beandefs) {
					// System.out.println("----" + bd.getBeanClassName());
					beanFactory.registerBeanDefinition(bd.getBeanClassName(), bd);
				}
			}
		}
	}

}
// /CLOVER:ON
