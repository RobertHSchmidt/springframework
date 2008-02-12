package org.springframework.config.java.process;

class ExternalBeanConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc) {
		return new ExternalBeanMethodMethodInterceptor(pc);
	}
}
