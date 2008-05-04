package org.springframework.config.java.model;


import static java.lang.String.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Primary;
import org.springframework.config.java.core.BeanFactoryFactory;
import org.springframework.config.java.type.Type;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Renders a given {@link ConfigurationModel} as bean definitions to be
 * registered on-the-fly with a given {@link BeanDefinitionRegistry}.
 * Modeled after the {@link BeanDefinitionReader} hierarchy, but could not extend
 * directly as {@link ConfigurationModel} is not a {@link Resource}
 *
 * @author Chris Beams
 */
public class ConfigurationModelBeanDefinitionReader {

	private static final Log logger = LogFactory.getLog(ConfigurationModelBeanDefinitionReader.class);

	private final BeanDefinitionRegistry registry;

	public ConfigurationModelBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int loadBeanDefinitions(ConfigurationModel model) {
		int initialBeanDefCount = registry.getBeanDefinitionCount();

		for(ConfigurationClass configClass : model.getAllConfigurationClasses())
			loadBeanDefinitionsForConfigurationClass(configClass);

		for(AspectClass aspectClass : model.getAspectClasses())
			loadBeanDefinitionsForAspectClass(aspectClass);

		return registry.getBeanDefinitionCount() - initialBeanDefCount;
	}

	private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass) {
		loadBeanDefinitionsForDeclaringClass(configClass.getDeclaringClass());

		String configClassName = configClass.getName();

		doLoadBeanDefinitionForConfigurationClass(configClassName);

		for(BeanMethod beanMethod : configClass.getBeanMethods())
			loadBeanDefinitionsForBeanMethod(configClass, configClassName, beanMethod);

		for(AutoBeanMethod autoBeanMethod : configClass.getAutoBeanMethods())
			loadBeanDefinitionsForAutoBeanMethod(configClass, configClassName, autoBeanMethod);
	}

	private void doLoadBeanDefinitionForConfigurationClass(String configClassName) {
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		// mark this bean def with metadata indicating that it is a configuration bean
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));

		// @Configuration classes' bean names are always their fully-qualified classname
		registry.registerBeanDefinition(configClassName, configBeanDef);
	}

	private void loadBeanDefinitionsForBeanMethod(ConfigurationClass configClass,
	                                              String configClassName,
	                                              BeanMethod beanMethod) {
		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setFactoryBeanName(configClassName);
		beanDef.setFactoryMethodName(beanMethod.getName());

		// consider autowiring
		if(beanMethod.getMetadata().autowire() != AnnotationUtils.getDefaultValue(Bean.class, "autowire"))
			beanDef.setAutowireMode(beanMethod.getMetadata().autowire().value());
		else
			if(configClass.getMetadata().defaultAutowire() != AnnotationUtils.getDefaultValue(Configuration.class, "defaultAutowire"))
				beanDef.setAutowireMode(configClass.getMetadata().defaultAutowire().value());

		// consider aliases
		for(String alias : beanMethod.getMetadata().aliases())
			// TODO: need to calculate bean name here, based on any naming strategy in the mix
			registry.registerAlias(beanMethod.getName(), alias);

		if(beanMethod.getMetadata().primary() == Primary.TRUE)
			beanDef.setPrimary(true);

		// TODO: plug in NamingStrategy here
		registry.registerBeanDefinition(beanMethod.getName(), beanDef);
	}

	private void loadBeanDefinitionsForAutoBeanMethod(ConfigurationClass configClass,
	                                                  String configClassName,
	                                                  AutoBeanMethod autoBeanMethod) {

		Type returnType = autoBeanMethod.getReturnType();
		/*
		Type returnType = autoBeanMethod.getReturnType();

		if (returnType.isInterface())
			throw new BeanDefinitionStoreException("Cannot use AutoBean of interface type " + m.getReturnType()
					+ ": don't know what class to instantiate; processing @AutoBean method " + m);
		*/

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(returnType.getName());
		beanDef.setAutowireMode(autoBeanMethod.getMetadata().autowire().value());

		registry.registerBeanDefinition(autoBeanMethod.getName(), beanDef);
	}

	private void loadBeanDefinitionsForAspectClass(AspectClass aspectClass) {
		String className = aspectClass.getName();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(className);

		// @Aspect classes' bean names are always their fully-qualified classname
		// don't overwrite any existing bean definition (in the case of an @Aspect @Configuration)
		if(!registry.containsBeanDefinition(className))
			registry.registerBeanDefinition(className, beanDef);
	}

	private void loadBeanDefinitionsForDeclaringClass(ConfigurationClass declaringClass) {
		if(declaringClass == null)
			return;

		logger.info(format("Found declaring class [%s] on configClass [%s]", declaringClass, declaringClass));

		BeanFactory parentBF;
		String factoryName = BeanFactoryFactory.class.getName();
		if(((ConfigurableListableBeanFactory)registry).containsBeanDefinition(factoryName))
			parentBF = (BeanFactory) ((ConfigurableListableBeanFactory)registry).getBean(factoryName, new Object[] { declaringClass.getName() });
		else
			parentBF = new DefaultListableBeanFactory();

		((ConfigurableListableBeanFactory)registry).setParentBeanFactory(parentBF);

		// TODO: test for the case where more than one configuration class has a declaring class - this should be illegal
		// because it would result in setParentBeanFactory being called more than once.
		// note that this violation should be detected at model validation time, not at rendering time - that's too late.
	}

}
