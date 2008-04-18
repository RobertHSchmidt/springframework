package org.springframework.config.java.model;


import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Renders a given {@link JavaConfigurationModel} as bean definitions to be registered on-the-fly
 * within a given {@link BeanDefinitionRegistry}.
 *
 * @author Chris Beams
 */
public class BeanDefinitionJavaConfigurationModelRenderer {

	private final BeanDefinitionRegistry registry;

	public BeanDefinitionJavaConfigurationModelRenderer(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int renderModel(JavaConfigurationModel model) {
		int initialBeanDefCount = registry.getBeanDefinitionCount();

		for(ConfigurationClass configClass : model.getConfigurationClasses()) {
			String configClassName = configClass.getClassName();
			RootBeanDefinition configBeanDef = new RootBeanDefinition();
			configBeanDef.setBeanClassName(configClassName);
			// @Configuration classes' bean names are always their fully-qualified classname
			registry.registerBeanDefinition(configClassName, configBeanDef);

			for(BeanMethod beanMethod : configClass.getBeanMethods()) {
				RootBeanDefinition beanDef = new RootBeanDefinition();
				beanDef.setFactoryBeanName(configClassName);
				beanDef.setFactoryMethodName(beanMethod.getName());
				// TODO: plug in NamingStrategy here
				registry.registerBeanDefinition(beanMethod.getName(), beanDef);
			}
		}

		return registry.getBeanDefinitionCount() - initialBeanDefCount;
	}

}