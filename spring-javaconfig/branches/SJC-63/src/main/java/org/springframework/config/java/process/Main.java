package org.springframework.config.java.process;

import static java.lang.String.format;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.core.ProcessingContext;

public class Main {
}

interface Reactor {
	void sourceEvent(Event event);

	void sourceBeanMethodEvent(BeanMethodEvent beanMethodEvent);

	boolean isConfigClass(Class<?> configurationClass);

	void sourceClassEvent(ClassEvent classEvent);

	void sourceMethodEvent(MethodEvent event);
}

interface Event {
	Object getSource();
}

abstract class EventSupport implements Event {
	protected final Object source;

	final ProcessingContext processingContext;

	EventSupport(Object source, ProcessingContext processingContext) {
		this.source = source;
		this.processingContext = processingContext;
	}

	public Object getSource() {
		return source;
	}
}

class MethodEvent extends EventSupport {

	final Method method;

	final Class<?> clazz;

	String configurationBeanName;

	MethodEvent(Object source, Class<?> clazz, Method method, ProcessingContext processingContext) {
		super(source, processingContext);
		this.clazz = clazz;
		this.method = method;
	}

	@Override
	public String toString() {
		return format("{%s:source=%s,method=%s.%s}", this.getClass().getSimpleName(), source, method
				.getDeclaringClass().getSimpleName(), method.getName());
	}

}

class BeanMethodEvent extends MethodEvent {

	final Bean beanAnnotation;

	final BeanDefinitionRegistration beanDefinitionRegistration;

	BeanMethodEvent(Object source, Class<?> clazz, Method method, Bean beanAnnotation, BeanDefinitionRegistration bdr,
			ProcessingContext processingContext) {
		super(source, clazz, method, processingContext);
		this.beanAnnotation = beanAnnotation;
		this.beanDefinitionRegistration = bdr;
	}

}

class ClassEvent extends EventSupport {

	final Class<?> clazz;

	String configurationBeanName;

	public ClassEvent(Object source, Class<?> clazz, ProcessingContext processingContext) {
		super(source, processingContext);
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return format("{%s:source=%s,class=%s}", this.getClass().getSimpleName(), source, clazz.getName());
	}
}
