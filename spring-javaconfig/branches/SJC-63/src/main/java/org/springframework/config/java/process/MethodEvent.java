package org.springframework.config.java.process;

import java.lang.reflect.Method;

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
		return String.format("{%s:source=%s,method=%s.%s}", this.getClass().getSimpleName(), source, method
				.getDeclaringClass().getSimpleName(), method.getName());
	}

}
