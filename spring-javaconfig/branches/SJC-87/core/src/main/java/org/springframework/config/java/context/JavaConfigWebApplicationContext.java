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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.config.java.process.LegacyConfigurationPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
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
 * TODO: This class is almost wholly copy-and-pasted from {@link JavaConfigApplicationContext}.
 * Because it the two classes must maintain mutually exclusive ancestries, acheiving reuse is
 * quite challenging. Consider a code-generation approach for JCWAC?
 *
 * @author Chris Beams
 */
public class JavaConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext
                                             implements ConfigurableJavaConfigApplicationContext {

	private Log log = LogFactory.getLog(getClass());

	private final ClassPathScanningCandidateComponentProvider scanner =
		new ClassPathScanningConfigurationProviderFactory().getProvider(this);

	private ArrayList<Class<?>> configClasses = new ArrayList<Class<?>>();

	private ArrayList<String> basePackages = new ArrayList<String>();

	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
		initConfigLocations();
	}

	@Override
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		BeanNamingStrategy namingStrategy = new MethodNameStrategy(); // default
		new InternalBeanFactoryEstablishingBeanFactoryPostProcessor(this).postProcessBeanFactory(beanFactory);
		new ConfigurationClassParsingBeanFactoryPostProcessor(namingStrategy).postProcessBeanFactory(beanFactory);
		new ConfigurationEnhancingBeanFactoryPostProcessor(namingStrategy).postProcessBeanFactory(beanFactory);
		super.invokeBeanFactoryPostProcessors(beanFactory);
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

	/**
	 * Loads any specified {@link Configuration @Configuration} classes and {@link Aspect @Aspect} classes
	 * as bean definitions within this context's BeanFactory for later processing by {@link LegacyConfigurationPostProcessor}
	 * @see #JavaConfigApplicationContext(Class...)
	 * @see #addConfigClasses(Class...)
	 * @see #addAspectClasses(Class...)
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		for(Class<?> configClass : configClasses)
			loadBeanDefinitionForConfigurationClass(beanFactory, configClass);

		for(String basePackage : basePackages)
			loadBeanDefinitionsForBasePackage(beanFactory, basePackage);
	}

	private void loadBeanDefinitionForConfigurationClass(DefaultListableBeanFactory beanFactory, Class<?> configClass) {
		String configBeanName = configClass.getName(); // TODO: {naming strategy} should end in # mark?
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configBeanName);
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		beanFactory.registerBeanDefinition(configBeanName, configBeanDef);
	}

	private void loadBeanDefinitionsForBasePackage(DefaultListableBeanFactory beanFactory, String basePackage) {
		for(BeanDefinition beanDef : scanner.findCandidateComponents(basePackage)) {
			ScannedGenericBeanDefinition configBeanDef = (ScannedGenericBeanDefinition) beanDef; // TODO: unfortunate cast
			String configBeanName = configBeanDef.getBeanClassName(); // TODO: {naming strategy}
			configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
			beanFactory.registerBeanDefinition(configBeanName, configBeanDef);
		}
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
		String[] configLocations = getConfigLocations();
		int nLocations = configLocations == null ? 0 : configLocations.length;
		String[] newConfigLocations = new String[nLocations+basePackages.length];

		for(int i=0; i<nLocations; i++)
			newConfigLocations[i] = configLocations[i];

		for(int i=0; i<basePackages.length; i++)
			newConfigLocations[nLocations+i] = basePackages[i];

		this.setConfigLocations(newConfigLocations);
	}

	public void setConfigClasses(Class<?>... classes) {
		throw new UnsupportedOperationException();
	}

	public void addAspectClasses(Class<?>... atAspectClasses) {
		throw new UnsupportedOperationException();
	}

}
