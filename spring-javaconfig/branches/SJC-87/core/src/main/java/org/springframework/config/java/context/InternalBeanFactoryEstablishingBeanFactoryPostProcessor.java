package org.springframework.config.java.context;

import static org.springframework.config.java.core.Constants.INTERNAL_BEAN_FACTORY_NAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
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


	public InternalBeanFactoryEstablishingBeanFactoryPostProcessor(AbstractApplicationContext ctx) {
		this.ctx = ctx;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) throws BeansException {
		if(log.isInfoEnabled())
			log.info("Establishing internal BeanFactory for " + externalBeanFactory);
		JavaConfigBeanFactory internalBeanFactory = createInternalBeanFactory(externalBeanFactory);
		externalBeanFactory.registerSingleton(INTERNAL_BEAN_FACTORY_NAME, internalBeanFactory);
	}

	private JavaConfigBeanFactory createInternalBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) {
		Assert.notNull(ctx, "ApplicationContext must be non-null");
		final DefaultJavaConfigBeanFactory internalBeanFactory = new DefaultJavaConfigBeanFactory(externalBeanFactory);

		final ConfigurableApplicationContext internalContext = new GenericApplicationContext(internalBeanFactory);

		// add a listener that triggers this child context to refresh when the parent refreshes.
		// this will cause any BeanPostProcessors / BeanFactoryPostProcessors to be invoked on
		// the beans in the child context.
		ctx.addApplicationListener(new ApplicationListener() {
			public void onApplicationEvent(ApplicationEvent event) {
				// only respond to ContextRefreshedEvents that are sourced from the parent context
				if(event.getSource() != ctx) return;
				if(!(event instanceof ContextRefreshedEvent)) return;

				//System.out.println("copying BFPPs 1");
        		for(Object bfpp : ctx.getBeanFactoryPostProcessors())
        			if(!(JavaConfigInternalPostProcessor.class.isAssignableFrom(bfpp.getClass())))
        				internalContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);

				//System.out.println("copying BFPPs 2");
        		for (Object bfpp : ctx.getBeansOfType(BeanFactoryPostProcessor.class).values()) {
        			String name = bfpp.getClass().getSimpleName();
        			//System.out.println("XYZ: " + name);
        			//if(!bfpp.getClass().equals(MyConfigurationPostProcessor.class))
    				// // internalContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
        		}

				//System.out.println("copying BPPs 1");
        		/*
        		for (String bppName : ctx.getBeanNamesForType(BeanPostProcessor.class)) {
        			BeanDefinition bppBeanDef = ctx.getBeanFactory().getBeanDefinition(bppName);
        			System.out.println("BPP: " + bppBeanDef);
        			internalBeanFactory.registerBeanDefinition(bppName, bppBeanDef);
        			//if(!bfpp.getClass().equals(MyConfigurationPostProcessor.class))
        				//internalContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
        		}
        		*/

        		/*
				System.out.println("copying BPPs 2");
				DefaultListableBeanFactory bf;
        		for (Object bpp : ((AbstractBeanFactory) ctx.getBeanFactory()).getBeanPostProcessors()) {
        			System.out.println("BPP: " + bpp);
        			internalContext.getBeanFactory().addBeanPostProcessor((BeanPostProcessor)bpp);
        			//if(!bfpp.getClass().equals(MyConfigurationPostProcessor.class))
        				//internalContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
        		}
        		*/
        		//internalContext.getBeanFactory().copyConfigurationFrom(ctx.getBeanFactory());

				//System.out.println("refreshing child");
				internalContext.setParent(ctx);
				internalContext.refresh();
			}
		});
		/*
		*/

		return internalBeanFactory;

	}

	public int getOrder() {
		return ORDER;
	}

}
