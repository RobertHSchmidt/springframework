package org.springframework.config.java.process;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.valuesource.CompositeValueSource;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

final class ProcessingContext {

	private static final Log log = LogFactory.getLog(ProcessingContext.class);

	public BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

	public ConfigurableListableBeanFactory owningBeanFactory;

	/**
	 * Used to hold Spring AOP advisors and other internal objects while
	 * processing configuration. Object added to this factory can still benefit
	 * from autowiring and other IoC container features, but are not visible
	 * externally.
	 */
	public BeanNameTrackingDefaultListableBeanFactory childFactory;

	public CompositeValueSource compositeValueSource = new CompositeValueSource();

	public int beanDefsGenerated = 0;

	public ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * If we are running in an ApplicationContext we create a child context, as
	 * well as a child BeanFactory, so that we can apply
	 * BeanFactoryPostProcessors to the child
	 */
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

	public void initialize(ConfigurableListableBeanFactory owningBeanFactory,
			ConfigurableApplicationContext owningApplicationContext,
			Iterable<BeanMethodReturnValueProcessor> returnValueProcessors) {

		if (owningBeanFactory != null)
			this.owningBeanFactory = owningBeanFactory;

		if (owningApplicationContext != null)
			this.owningApplicationContext = owningApplicationContext;

		this.returnValueProcessors = returnValueProcessors;

		doInitialize();
	}

	private void doInitialize() {
		if (owningApplicationContext != null)
			owningBeanFactory = owningApplicationContext.getBeanFactory();

		Assert.notNull(owningBeanFactory, "an owning factory bean is required");

		childFactory = new BeanNameTrackingDefaultListableBeanFactory(owningBeanFactory);

		if (owningApplicationContext != null && owningApplicationContext instanceof AbstractApplicationContext) {
			childApplicationContext = createChildApplicationContext(childFactory);
			copyBeanPostProcessors((AbstractApplicationContext) owningApplicationContext, childApplicationContext);
		}
	}

	private ConfigurableApplicationContext createChildApplicationContext(
			BeanNameTrackingDefaultListableBeanFactory childFactory) {
		final ConfigurableApplicationContext child = new GenericApplicationContext(childFactory,
				owningApplicationContext) {
			// TODO this override is a hack! Why is EventMulticaster null?
			@Override
			public void publishEvent(ApplicationEvent event) {
				if (event instanceof ContextRefreshedEvent)
					log.debug("suppressed " + event);
			}
		};

		// Piggyback on owning application context refresh
		owningApplicationContext.addApplicationListener(new ApplicationListener() {
			public void onApplicationEvent(ApplicationEvent event) {
				if (event instanceof ContextRefreshedEvent)
					child.refresh();
			}
		});

		return child;
	}

	private void copyBeanPostProcessors(AbstractApplicationContext source, ConfigurableApplicationContext dest) {
		// TODO: why do both of the iterations below? wouldn't one of the
		// two suffice?
		List<BeanFactoryPostProcessor> bfpps = new LinkedList<BeanFactoryPostProcessor>();

		Class<?> configurationPostProcessor;
		try {
			configurationPostProcessor = Class
					.forName("org.springframework.config.java.process.ConfigurationPostProcessor");
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		for (Object o : source.getBeansOfType(BeanFactoryPostProcessor.class).values())
			if (!(o.getClass().isAssignableFrom(configurationPostProcessor)))
				bfpps.add((BeanFactoryPostProcessor) o);

		for (Object o : source.getBeanFactoryPostProcessors())
			if (!(o.getClass().isAssignableFrom(configurationPostProcessor)))
				bfpps.add((BeanFactoryPostProcessor) o);

		// Add all BeanFactoryPostProcessors to the child context
		for (BeanFactoryPostProcessor bfpp : bfpps)
			dest.addBeanFactoryPostProcessor(bfpp);
	}

}