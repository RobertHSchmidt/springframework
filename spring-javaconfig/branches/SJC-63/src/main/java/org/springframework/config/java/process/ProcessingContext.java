package org.springframework.config.java.process;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.naming.BeanNamingStrategy;

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

	public ProcessingContext(BeanNamingStrategy beanNamingStrategy, ConfigurableListableBeanFactory owningBeanFactory,
			BeanNameTrackingDefaultListableBeanFactory childFactory) {
		this.beanNamingStrategy = beanNamingStrategy;
		this.owningBeanFactory = owningBeanFactory;
		this.childFactory = childFactory;
	}

}