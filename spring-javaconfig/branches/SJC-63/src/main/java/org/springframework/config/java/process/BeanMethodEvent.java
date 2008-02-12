package org.springframework.config.java.process;

import java.lang.reflect.Method;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;

class BeanMethodEvent extends MethodEvent {

	final Bean beanAnnotation;

	RootBeanDefinition rbd;

	final boolean hide;

	final String name;

	BeanMethodEvent(Object source, Class<?> clazz, Method method, Bean beanAnnotation, RootBeanDefinition rbd,
			boolean hide, String beanName, ProcessingContext processingContext) {
		super(source, clazz, method, processingContext);
		this.beanAnnotation = beanAnnotation;
		this.rbd = rbd;
		this.hide = hide;
		this.name = beanName;
	}

}
