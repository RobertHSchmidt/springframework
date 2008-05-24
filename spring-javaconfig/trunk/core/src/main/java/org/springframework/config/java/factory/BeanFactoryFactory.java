package org.springframework.config.java.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanFactoryFactory {
	static final String BEAN_NAME = BeanFactoryFactory.class.getName();

	abstract BeanFactory createBeanFactory(String className) throws Exception;

	void registerBeanDefinition(BeanDefinitionRegistry registry);
}
