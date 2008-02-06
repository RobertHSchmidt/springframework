package org.springframework.config.java.process;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
import org.springframework.config.java.valuesource.ValueSource;
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

	final BeanNamingStrategy beanNamingStrategy;

	final ConfigurableListableBeanFactory owningBeanFactory;

	final BeanNameTrackingDefaultListableBeanFactory childFactory;

	final CompositeValueSource compositeValueSource;

	int beanDefsGenerated = -1;

	final ResourceLoader resourceLoader;

	public ProcessingContext(BeanNamingStrategy beanNamingStrategy, ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory, CompositeValueSource compositeValueSource,
			ResourceLoader resourceLoader) {
		this.beanNamingStrategy = beanNamingStrategy;
		this.owningBeanFactory = owningBeanFactory;
		this.childFactory = childFactory;
		this.compositeValueSource = compositeValueSource;
		this.resourceLoader = resourceLoader;
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

}