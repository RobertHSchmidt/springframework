package org.springframework.config.java.process;

import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.enhancement.cglib.ExternalBeanMethodMethodInterceptor;
import org.springframework.config.java.enhancement.cglib.JavaConfigMethodInterceptor;

public class ExternalBeanConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc) {
		return new ExternalBeanMethodMethodInterceptor(pc);
	}
}
