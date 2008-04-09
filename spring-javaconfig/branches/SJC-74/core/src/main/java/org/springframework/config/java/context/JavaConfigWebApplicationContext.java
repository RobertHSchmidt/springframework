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
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.Assert;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;

/**
 * JavaConfig ApplicationContext implementation for use in the web tier. May be
 * supplied as the {@literal commandClass} parameter to Spring MVC's
 * DispatcherServlet
 * @see JavaConfigApplicationContext
 * @see org.springframework.web.context.WebApplicationContext
 * @see org.springframework.web.servlet.DispatcherServlet
 * 
 * @author Chris Beams
 */
public class JavaConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext
implements ConfigurableJavaConfigApplicationContext {

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
				if (ConfigurationProcessor.isConfigurationClass(cz)) {
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
				if (candidate != null && ConfigurationProcessor.isConfigurationClass(candidate)) {
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
			RootBeanDefinition beanDef = new RootBeanDefinition(cz, true);
			ConfigurationProcessor.processExternalValueConstructorArgs(beanDef, this);
			beanFactory.registerBeanDefinition(cz.getName(), beanDef);
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

	public void addConfigClass(Class<?> cls) {
		String[] configLocations = getConfigLocations();
		int nLocations = configLocations == null ? 0 : configLocations.length;
		String[] newConfigLocations = new String[nLocations+1];
		for(int i=0; i<nLocations; i++)
			newConfigLocations[i] = configLocations[i];
		newConfigLocations[newConfigLocations.length-1] = cls.getName();
		this.setConfigLocations(newConfigLocations);
	}

	public void setBasePackages(String... basePackages) {
		throw new UnsupportedOperationException();
	}

	public void setConfigClasses(Class<?>... classes) {
		throw new UnsupportedOperationException();
	}

}
