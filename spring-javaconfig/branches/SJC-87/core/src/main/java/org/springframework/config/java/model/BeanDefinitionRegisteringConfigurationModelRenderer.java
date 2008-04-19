package org.springframework.config.java.model;


import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Primary;

/**
 * Renders a given {@link ConfigurationModel} as bean definitions to be
 * registered on-the-fly with a given {@link BeanDefinitionRegistry}.
 *
 * @author Chris Beams
 */
public class BeanDefinitionRegisteringConfigurationModelRenderer {

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

		for(ConfigurationClass configClass : model.getConfigurationClasses()) {
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
				if(beanMethod.getBeanAnnotation().primary() == Primary.TRUE)
					beanDef.setPrimary(true);
				// TODO: plug in NamingStrategy here
				registry.registerBeanDefinition(beanMethod.getName(), beanDef);
			}
		}

		return registry.getBeanDefinitionCount() - initialBeanDefCount;
	}

}