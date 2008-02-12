package org.springframework.config.java.process;

import static java.lang.String.format;

import org.springframework.util.Assert;

class ClassEvent extends EventSupport {

	final Class<?> clazz;

	final String configurationBeanName;

	public ClassEvent(Object source, Class<?> configurationClass, ProcessingContext processingContext) {
		this(source, configurationClass, null, processingContext);
	}

	public ClassEvent(Object source, Class<?> configurationClass, String configurationBeanName,
			ProcessingContext processingContext) {
		super(source, processingContext);
		Assert.notNull(configurationClass, "configurationClass may not be null");
		this.clazz = configurationClass;
		if (configurationBeanName != null)
			this.configurationBeanName = configurationBeanName;
		else
			this.configurationBeanName = configurationClass.getName();
	}

	@Override
	public String toString() {
		return format("{%s:source=%s,class=%s}", this.getClass().getSimpleName(), source, clazz.getName());
	}
}
