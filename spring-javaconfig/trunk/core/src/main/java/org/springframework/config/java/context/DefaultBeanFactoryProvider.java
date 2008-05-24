package org.springframework.config.java.context;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.core.Constants;
import org.springframework.config.java.factory.BeanFactoryProvider;

public class DefaultBeanFactoryProvider implements BeanFactoryProvider {

	public void registerBeanDefinition(BeanDefinitionRegistry registry) {

		// register factory bean
		RootBeanDefinition factoryBean = new RootBeanDefinition();
		String factoryBeanName = this.getClass().getName();
		factoryBean.setBeanClassName(this.getClass().getName());
		factoryBean.addMetadataAttribute(new BeanMetadataAttribute(Constants.JAVA_CONFIG_IGNORE, true));

		registry.registerBeanDefinition(factoryBeanName, factoryBean);


		RootBeanDefinition bff = new RootBeanDefinition();
		bff.setFactoryBeanName(factoryBeanName);
		bff.setFactoryMethodName("createBeanFactory");
		bff.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		bff.addMetadataAttribute(new BeanMetadataAttribute(Constants.JAVA_CONFIG_IGNORE, true));

		registry.registerBeanDefinition(BeanFactoryProvider.BEAN_NAME, bff);
	}

	public BeanFactory createBeanFactory(String className) throws Exception {
		return new JavaConfigApplicationContext(Class.forName(className));
	}


}
