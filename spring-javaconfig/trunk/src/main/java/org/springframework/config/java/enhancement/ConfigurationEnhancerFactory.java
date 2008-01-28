package org.springframework.config.java.enhancement;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.ValueSource;

/**
 * @author Chris Beams
 */
public interface ConfigurationEnhancerFactory {

	public ConfigurationEnhancer getConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, BeanNamingStrategy beanNamingStrategy,
			MethodBeanWrapper beanWrapper, ValueSource valueSource);
}
