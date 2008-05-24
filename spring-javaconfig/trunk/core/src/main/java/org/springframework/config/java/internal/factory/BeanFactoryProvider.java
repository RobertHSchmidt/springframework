package org.springframework.config.java.internal.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

public interface BeanFactoryProvider {
	static final String BEAN_NAME = BeanFactoryProvider.class.getName();

	abstract BeanFactory createBeanFactory(String className) throws Exception;

	void registerBeanDefinition(BeanDefinitionRegistry registry);
}
