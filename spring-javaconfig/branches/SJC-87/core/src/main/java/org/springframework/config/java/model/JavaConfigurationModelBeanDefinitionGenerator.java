package org.springframework.config.java.model;


import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Populates a given {@link BeanDefinitionRegistry} based on metadata within a
 * given {@link JavaConfigurationModel}
 *
 * @author Chris Beams
 */
public class JavaConfigurationModelBeanDefinitionGenerator {

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int generateBeanDefinitionsFromModel(BeanDefinitionRegistry registry, JavaConfigurationModel model) {

		int initialBeanDefCount = registry.getBeanDefinitionNames().length;

		for(ConfigurationClass configClass : model.getConfigurationClasses()) {
			String configClassName = configClass.getClassName();
			RootBeanDefinition configBeanDef = new RootBeanDefinition();
			configBeanDef.setBeanClassName(configClassName);
			// @Configuration classes' bean names are always their fully-qualified classname
			registry.registerBeanDefinition(configClassName, configBeanDef);

			for(BeanMethod beanMethod : configClass.getBeanMethods()) {
				RootBeanDefinition beanDef = new RootBeanDefinition();
				beanDef.setFactoryBeanName(configClassName);
				beanDef.setFactoryMethodName(beanMethod.getMethodName());
				// TODO: plug in NamingStrategy here
				registry.registerBeanDefinition(beanMethod.getMethodName(), beanDef);
			}
		}

		return registry.getBeanDefinitionNames().length - initialBeanDefCount;
	}

}