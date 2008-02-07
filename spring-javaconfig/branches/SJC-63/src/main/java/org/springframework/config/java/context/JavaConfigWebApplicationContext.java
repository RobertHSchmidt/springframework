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

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.process.ConfigurationUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.Assert;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

/**
 * Fashioned after {@link JavaConfigApplicationContext}
 * 
 * <p/>TODO: Finish document
 * 
 * @author Chris Beams
 */
public class JavaConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext implements
		TypeSafeBeanFactory {

	private Log log = LogFactory.getLog(getClass());

	private final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningConfigurationProviderFactory()
			.getProvider(this);

	private ArrayList<Class<?>> configClasses = new ArrayList<Class<?>>();

	private ArrayList<String> basePackages = new ArrayList<String>();

	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
		initConfigLocations();
		processAnyOuterClasses();
		registerDefaultPostProcessors();
	}

	protected void initConfigLocations() {
		Assert.notEmpty(getConfigLocations(), "configLocations property has not been set");
		for (String location : getConfigLocations()) {
			try {
				Class<?> cz = Class.forName(location);
				if (ConfigurationUtils.isConfigurationClass(cz)) {
					configClasses.add(cz);
				}
				else {
					String message = "[%s] is not a valid configuration class. "
							+ "Perhaps you forgot to annotate your bean creation methods with @Bean?";
					log.warn(format(message, cz));
				}
			}
			catch (ClassNotFoundException ex) {
				basePackages.add(location);
			}
		}
	}

	private void processAnyOuterClasses() {
		Class<?> outerConfig = null;
		if (configClasses != null && configClasses.size() > 0) {
			for (Class<?> configClass : configClasses) {
				Class<?> candidate = configClass.getDeclaringClass();
				if (candidate != null && ConfigurationUtils.isConfigurationClass(candidate)) {
					if (outerConfig != null) {
						// TODO: throw a better exception
						throw new RuntimeException("cannot specify more than one inner configuration class");
					}
					outerConfig = candidate;
				}
			}
		}

		if (outerConfig != null)
			this.setParent(new JavaConfigApplicationContext(outerConfig));
	}

	/**
	 * Processes contents of <var>configLocations</var>, setting the values of
	 * configClasses and basePackages appropriately.
	 * 
	 * @throws IllegalArgumentException if the <code>configLocations</code>
	 * array is null, contains any null elements, or contains names of any
	 * classes that cannot be found
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {
		for (Class<?> cz : configClasses) {
			beanFactory.registerBeanDefinition(cz.getName(), new RootBeanDefinition(cz, true));
		}

		for (String basePackage : basePackages) {
			Set<BeanDefinition> beandefs = scanner.findCandidateComponents(basePackage);
			if (beandefs.size() > 0) {
				for (BeanDefinition bd : beandefs) {
					beanFactory.registerBeanDefinition(bd.getBeanClassName(), bd);
				}
			}
			else {
				String message = "[%s] is either specifying a configuration class that does not exist "
						+ "or is a base package pattern that does not match any configuration classes. "
						+ "No bean definitions were found as a result of processing this configLocation";
				log.warn(format(message, basePackage));
			}
		}
	}

	/**
	 * Register the default post processors used for parsing Spring classes.
	 * 
	 * @see JavaConfigBeanFactoryPostProcessorRegistry
	 */
	protected void registerDefaultPostProcessors() {
		new JavaConfigBeanFactoryPostProcessorRegistry().addAllPostProcessors(this);
	}

	public <T> T getBean(Class<T> type) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
	}

	public <T> T getBean(Class<T> type, String beanName) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type, beanName);
	}

}
