package org.springframework.config.java.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.ValueSource;

public final class ConfigurationEnhancerFactory {
	public static ConfigurationEnhancer getConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, BeanNamingStrategy beanNamingStrategy,
			MethodBeanWrapper beanWrapper, ValueSource valueSource) {
		return new CglibConfigurationEnhancer(owningBeanFactory, childFactory, beanNamingStrategy, beanWrapper,
				valueSource);
	}
}
