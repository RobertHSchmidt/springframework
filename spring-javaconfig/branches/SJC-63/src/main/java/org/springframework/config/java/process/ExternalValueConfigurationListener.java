package org.springframework.config.java.process;

class ExternalValueConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc) {
		return new ExternalValueMethodMethodInterceptor(pc);
	}
}
