package org.springframework.config.java.process;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;

class ProcessingContext {

	private static InheritableThreadLocal<ProcessingContext> instance = new InheritableThreadLocal<ProcessingContext>() {
		@Override
		protected ProcessingContext initialValue() {
			throw new IllegalStateException("ProcessingContext has not yet been populated");
		}
	};

	static ProcessingContext getCurrentContext() {
		return instance.get();
	}

	static void setCurrentContext(ProcessingContext context) {
		instance.set(context);
	}

	BeanNamingStrategy beanNamingStrategy;

	ConfigurableListableBeanFactory owningBeanFactory;

	BeanNameTrackingDefaultListableBeanFactory childFactory;

	CompositeValueSource compositeValueSource;

	int beanDefsGenerated = -1;

	ResourceLoader resourceLoader;

	ConfigurationEnhancer configurationEnhancer;

	ConfigurableApplicationContext childApplicationContext;

	public ProcessingContext() {
	}

	public void registerBeanDefinition(String name, BeanDefinition bd, boolean hide) {
		if (hide)
			childFactory.registerBeanDefinition(name, bd);
		else
			((BeanDefinitionRegistry) owningBeanFactory).registerBeanDefinition(name, bd);
	}

	public void addValueSource(ValueSource vs) {
		compositeValueSource.add(vs);
	}

	public BeanFactory getChildBeanFactory() {
		return (childApplicationContext != null) ? childApplicationContext : childFactory;
	}

	public void registerSingleton(String name, Object o, boolean hide) {
		if (hide)
			childFactory.registerSingleton(name, o);
		else
			owningBeanFactory.registerSingleton(name, o);
	}

}