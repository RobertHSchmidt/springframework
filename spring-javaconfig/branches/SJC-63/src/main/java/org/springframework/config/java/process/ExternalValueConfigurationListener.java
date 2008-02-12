package org.springframework.config.java.process;

import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.enhancement.cglib.ExternalValueMethodMethodInterceptor;
import org.springframework.config.java.enhancement.cglib.JavaConfigMethodInterceptor;

public class ExternalValueConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public JavaConfigMethodInterceptor getMethodInterceptor(ProcessingContext pc) {
		return new ExternalValueMethodMethodInterceptor(pc);
	}
}
