package org.springframework.config.java.process;

import static java.lang.String.format;
import static org.springframework.config.java.core.Constants.INTERNAL_BEAN_FACTORY_NAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.factory.BeanFactoryFactory;
import org.springframework.config.java.factory.DefaultJavaConfigBeanFactory;
import org.springframework.config.java.factory.JavaConfigBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;

public class InternalBeanFactoryEstablishingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

	public static final int ORDER = Ordered.HIGHEST_PRECEDENCE;

	private static final Log log = LogFactory.getLog(InternalBeanFactoryEstablishingBeanFactoryPostProcessor.class);

	private final AbstractApplicationContext ctx;

	private final BeanFactoryFactory bff;

	private BeanNamingStrategy beanNamingStrategy;


	public InternalBeanFactoryEstablishingBeanFactoryPostProcessor(AbstractApplicationContext ctx, BeanFactoryFactory bff) {
		this.ctx = ctx;
		this.bff = bff;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) throws BeansException {
		if(log.isInfoEnabled())
			log.info("Establishing internal BeanFactory for " + externalBeanFactory);
		JavaConfigBeanFactory internalBeanFactory = createInternalBeanFactory(externalBeanFactory);
		externalBeanFactory.registerSingleton(INTERNAL_BEAN_FACTORY_NAME, internalBeanFactory);
	}

	private JavaConfigBeanFactory createInternalBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) {
		Assert.notNull(ctx, "ApplicationContext must be non-null");
		final DefaultJavaConfigBeanFactory internalBeanFactory = new DefaultJavaConfigBeanFactory(externalBeanFactory, bff);

		if(beanNamingStrategy != null)
			internalBeanFactory.setBeanNamingStrategy(beanNamingStrategy);

		for (String bppName : externalBeanFactory.getBeanNamesForType(BeanPostProcessor.class))
			internalBeanFactory.registerSingleton(bppName, externalBeanFactory.getBean(bppName));

		final AbstractApplicationContext internalContext = new GenericApplicationContext(internalBeanFactory);
		internalContext.setDisplayName("JavaConfig internal application context");

		// add a listener that triggers this child context to refresh when the parent refreshes.
		// this will cause any BeanPostProcessors / BeanFactoryPostProcessors to be beansInvokedFor on
		// the beans in the child context.
		ctx.addApplicationListener(new ChildContextRefreshingListener(ctx, internalContext));

		return internalBeanFactory;
	}

	public int getOrder() {
		return ORDER;
	}

	public void setBeanNamingStrategy(BeanNamingStrategy beanNamingStrategy) {
		this.beanNamingStrategy = beanNamingStrategy;
	}

}

/**
 * Listens for {@link ContextRefreshedEvent ContextRefreshedEvents} sourced from a parent application context
 * and propagates that event to the child.  Just prior to refreshing the child, searches for and adds any
 * {@link BeanFactoryPostProcessor} and {@link BeanPostProcessor} objects found in the parent context's
 * {@link BeanFactory}
 *
 * @author Chris Beams
 */
class ChildContextRefreshingListener implements ApplicationListener {

	private static final Log logger = LogFactory.getLog(ChildContextRefreshingListener.class);
	private final AbstractApplicationContext parent;
	private final AbstractApplicationContext child;

	public ChildContextRefreshingListener(AbstractApplicationContext parent, AbstractApplicationContext child) {
		this.parent = parent;
		this.child = child;
	}

	public void onApplicationEvent(ApplicationEvent event) {

		// only respond to ContextRefreshedEvents that are sourced from the parent context
		if(event.getSource() != parent) return;
		if(!(event instanceof ContextRefreshedEvent)) return;

		if(logger.isDebugEnabled())
			logger.debug(format("Caught ContextRefreshedEvent from parent application context [%s], now refreshing [%s]",
				parent.getDisplayName(), child.getDisplayName()));

		copyBeanFactoryPostProcessors();

		copyBeanPostProcessors();

		//System.out.println("refreshing child");
		child.setParent(parent);
		child.refresh();
	}

	private void copyBeanPostProcessors() {
		//child.getBeanFactory().copyConfigurationFrom(parent.getBeanFactory());
		/*
		for (String bppName : parent.getBeanNamesForType(BeanPostProcessor.class)) {
			BeanDefinition bppBeanDef = parent.getBeanFactory().getBeanDefinition(bppName);
			System.out.println("BPP: " + bppBeanDef);
			child.registerBeanDefinition(bppName, bppBeanDef);
		}
		*/

		/*
		DefaultListableBeanFactory bf;
		for (Object bpp : ((AbstractBeanFactory) parent.getBeanFactory()).getBeanPostProcessors()) {
			System.out.println("BPP: " + bpp);
			child.getBeanFactory().addBeanPostProcessor((BeanPostProcessor)bpp);
			//if(!bfpp.getClass().equals(MyConfigurationPostProcessor.class))
				//child.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
		}
		*/

	}

	private void copyBeanFactoryPostProcessors() {
		// both of the following loops are necessary because BFPPs may have been added as singletons or as bean definitions
		// we must iterate over both possibilities in order to find all potential post processors.
		for(Object bfpp : parent.getBeanFactoryPostProcessors())
			doCopyBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);

		for (Object bfpp : parent.getBeansOfType(BeanFactoryPostProcessor.class).values())
			doCopyBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
	}

	private void doCopyBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		// TODO: {eager classloading} potential classloading trouble for Spring IDE: do this with a ClassReader
		if(!(JavaConfigInternalPostProcessor.class.isAssignableFrom(postProcessor.getClass()))) {
			logger.debug(String.format("copying BeanFactoryPostProcessor %s to child context %s", postProcessor, child));
			child.addBeanFactoryPostProcessor(postProcessor);
		}
	}

}
