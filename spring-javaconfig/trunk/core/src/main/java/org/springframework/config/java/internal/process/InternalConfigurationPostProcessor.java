package org.springframework.config.java.internal.process;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.internal.factory.BeanFactoryProvider;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.support.AbstractApplicationContext;

public class InternalConfigurationPostProcessor implements BeanFactoryPostProcessor {

	private final AbstractApplicationContext enclosingContext;
	private final BeanNamingStrategy beanNamingStrategy;
	private final BeanFactoryProvider beanFactoryProvider;

	public InternalConfigurationPostProcessor(AbstractApplicationContext enclosingContext, BeanNamingStrategy beanNamingStrategy, BeanFactoryProvider beanFactoryProvider) {
		this.enclosingContext = enclosingContext;
		this.beanNamingStrategy = beanNamingStrategy;
		this.beanFactoryProvider = beanFactoryProvider;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		InternalBeanFactoryEstablishingBeanFactoryPostProcessor iBPP =
			new InternalBeanFactoryEstablishingBeanFactoryPostProcessor(enclosingContext, beanFactoryProvider);
		if(beanNamingStrategy != null)
			iBPP.setBeanNamingStrategy(beanNamingStrategy);
		iBPP.postProcessBeanFactory(beanFactory);
		new ConfigurationClassParsingBeanFactoryPostProcessor().postProcessBeanFactory(beanFactory);
		new ConfigurationEnhancingBeanFactoryPostProcessor().postProcessBeanFactory(beanFactory);
	}

}
