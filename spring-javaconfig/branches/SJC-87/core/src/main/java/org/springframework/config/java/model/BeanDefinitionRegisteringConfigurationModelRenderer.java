package org.springframework.config.java.model;


import static java.lang.String.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Primary;
import org.springframework.config.java.core.BeanFactoryFactory;

/**
 * Renders a given {@link ConfigurationModel} as bean definitions to be
 * registered on-the-fly with a given {@link BeanDefinitionRegistry}.
 *
 * @author Chris Beams
 */
public class BeanDefinitionRegisteringConfigurationModelRenderer {

	private static final Log log = LogFactory.getLog(BeanDefinitionRegisteringConfigurationModelRenderer.class);

	private final BeanDefinitionRegistry registry;

	public BeanDefinitionRegisteringConfigurationModelRenderer(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int render(ConfigurationModel model) {
		int initialBeanDefCount = registry.getBeanDefinitionCount();

		for(ConfigurationClass configClass : model.getAllConfigurationClasses())
			renderClass(configClass);

		for(AspectClass aspectClass : model.getAspectClasses())
			renderAspectClass(aspectClass);

		return registry.getBeanDefinitionCount() - initialBeanDefCount;
	}

	private void renderAspectClass(AspectClass aspectClass) {
		String className = aspectClass.getName();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(className);

		// @Aspect classes' bean names are always their fully-qualified classname
		// don't overwrite any existing bean definition (in the case of an @Aspect @Configuration)
		if(!registry.containsBeanDefinition(className))
			registry.registerBeanDefinition(className, beanDef);
	}

	private void renderClass(ConfigurationClass configClass) {

		renderDeclaringClass(configClass.getDeclaringClass());

		String configClassName = configClass.getName();

		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		// mark this bean def with metadata indicating that it is a configuration bean
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));


		// @Configuration classes' bean names are always their fully-qualified classname
		registry.registerBeanDefinition(configClassName, configBeanDef);

		for(BeanMethod beanMethod : configClass.getBeanMethods()) {
			RootBeanDefinition beanDef = new RootBeanDefinition();
			beanDef.setFactoryBeanName(configClassName);
			beanDef.setFactoryMethodName(beanMethod.getName());

			// consider autowire metadata TODO: also consider @Bean-level autowire settings
			Autowire defaultAutowire = configClass.getMetadata().defaultAutowire();
			if(defaultAutowire != Autowire.INHERITED)
				beanDef.setAutowireMode(defaultAutowire.value());

			// consider aliases
			for(String alias : beanMethod.getMetadata().aliases())
				// TODO: need to calculate bean name here, based on any naming strategy in the mix
				registry.registerAlias(beanMethod.getName(), alias);


			if(beanMethod.getMetadata().primary() == Primary.TRUE)
				beanDef.setPrimary(true);
			// TODO: plug in NamingStrategy here
			registry.registerBeanDefinition(beanMethod.getName(), beanDef);
		}
	}

	private void renderDeclaringClass(ConfigurationClass declaringClass) {
		if(declaringClass == null) return;

		log.info(format("Found declaring class [%s] on configClass [%s]", declaringClass, declaringClass));

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
