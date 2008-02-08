package org.springframework.config.java.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public final class ProcessingContext {

	private static final Log log = LogFactory.getLog(ProcessingContext.class);

	public BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

	public ConfigurableListableBeanFactory owningBeanFactory;

	public BeanNameTrackingDefaultListableBeanFactory childFactory;

	public CompositeValueSource compositeValueSource = new CompositeValueSource();

	public int beanDefsGenerated = -1;

	public ResourceLoader resourceLoader = new DefaultResourceLoader();

	public ConfigurableApplicationContext childApplicationContext;

	public Iterable<BeanMethodReturnValueProcessor> returnValueProcessors;

	/**
	 * Non-null if we are running in an ApplicationContext and need to ensure
	 * that BeanFactoryProcessors from the parent apply to the child
	 */
	public ConfigurableApplicationContext owningApplicationContext;

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