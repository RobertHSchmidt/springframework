package org.springframework.config.java.internal.factory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;

public interface JavaConfigBeanFactory extends ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	boolean isCurrentlyInCreation(String beanName);

	/**
	 * Overridden to exploit covariant return type
	 */
	DefaultListableBeanFactory getParentBeanFactory();

	void registerSingleton(String beanName, Object bean, BeanVisibility visibility);

	void registerBeanDefinition(String beanName, BeanDefinition beanDef, BeanVisibility visibility);

	void registerAlias(String beanName, String alias, BeanVisibility visibility);

	boolean containsBeanDefinition(String className, BeanVisibility visibility);

	public BeanNamingStrategy getBeanNamingStrategy();
}