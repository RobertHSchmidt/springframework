package org.springframework.config.java.process;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.context.DefaultBeanFactoryProvider;
import org.springframework.config.java.internal.process.InternalConfigurationPostProcessor;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

public class ConfigurationPostProcessor implements Ordered, ApplicationContextAware, BeanFactoryPostProcessor {

	private AbstractApplicationContext ctx;
	private BeanNamingStrategy beanNamingStrategy;

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		new ConfigurationBeanDefinitionDecoratingBeanFactoryPostProcessor().postProcessBeanFactory(beanFactory);

		InternalConfigurationPostProcessor icpp = new InternalConfigurationPostProcessor(ctx, beanNamingStrategy, new DefaultBeanFactoryProvider());
		icpp.addIgnoredBeanPostProcessor(this.getClass().getName());
		icpp.postProcessBeanFactory(beanFactory);
	}

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		Assert.isInstanceOf(AbstractApplicationContext.class, ctx);
		this.ctx = (AbstractApplicationContext) ctx;
	}

	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	/**
	 * In place for backward-compatibility with existing milestone releases
	 * TODO: [breaks-backward-compat] (or will when removed)
	 * @deprecated Use {@link #setBeanNamingStrategy(BeanNamingStrategy)} instead
	 */
	@Deprecated
	public void setNamingStrategy(BeanNamingStrategy namingStrategy) {
		setBeanNamingStrategy(namingStrategy);
	}

	public void setBeanNamingStrategy(BeanNamingStrategy namingStrategy) {
		this.beanNamingStrategy = namingStrategy;
	}

}
