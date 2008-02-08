package org.springframework.config.java.core;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

public class ProcessingContext {

	private static InheritableThreadLocal<ProcessingContext> instance = new InheritableThreadLocal<ProcessingContext>() {
		@Override
		protected ProcessingContext initialValue() {
			throw new IllegalStateException("ProcessingContext has not yet been populated");
		}
	};

	public static ProcessingContext getCurrentContext() {
		return instance.get();
	}

	public static void setCurrentContext(ProcessingContext context) {
		instance.set(context);
	}

	public BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

	public ConfigurableListableBeanFactory owningBeanFactory;

	public BeanNameTrackingDefaultListableBeanFactory childFactory;

	public CompositeValueSource compositeValueSource;

	public int beanDefsGenerated = -1;

	public ResourceLoader resourceLoader = new DefaultResourceLoader();

	public ConfigurableApplicationContext childApplicationContext;

	public Iterable<BeanMethodReturnValueProcessor> returnValueProcessors;

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