package org.springframework.config.java.enhancement;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.ValueSource;

/**
 * {@link ConfigurationEnhancerFactory} designed to hide the implementation
 * details of returned enhancer instances. Currently hard-coded to return a
 * CGLIB-based enhancer. This class is not designed to be user-configurable, but
 * rather exists to serve the architectural goal of information hiding and
 * keeping the public API small.
 * 
 * @author Chris Beams
 */
public final class DefaultConfigurationEnhancerFactory implements ConfigurationEnhancerFactory {

	/**
	 * Returns the default enhancer implementation (currently CGLIB). No caching
	 * is (or will be) provided by this method; a new enhancer instance will be
	 * returned on each call.
	 * 
	 * @param owningBeanFactory
	 * @param childFactory
	 * @param beanNamingStrategy
	 * @param beanWrapper
	 * @param valueSource
	 * 
	 * @return ConfigurationEnhancer instance
	 */
	public ConfigurationEnhancer getConfigurationEnhancer(ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, BeanNamingStrategy beanNamingStrategy,
			MethodBeanWrapper beanWrapper, ValueSource valueSource) {
		return new CglibConfigurationEnhancer(owningBeanFactory, childFactory, beanNamingStrategy, beanWrapper,
				valueSource);
	}
}
