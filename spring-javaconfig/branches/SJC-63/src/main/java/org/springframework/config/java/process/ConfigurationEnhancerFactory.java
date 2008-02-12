package org.springframework.config.java.process;

import java.util.List;

import org.springframework.config.java.core.ProcessingContext;
import org.springframework.config.java.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.enhancement.cglib.CglibConfigurationEnhancer;

public class ConfigurationEnhancerFactory {

	private final List<ConfigurationListener> configurationListeners;

	public ConfigurationEnhancerFactory(List<ConfigurationListener> configurationListeners) {
		this.configurationListeners = configurationListeners;
	}

	public ConfigurationEnhancer createNewEnhancer(ProcessingContext pc) {
		CglibConfigurationEnhancer enhancer = new CglibConfigurationEnhancer();
		for (ConfigurationListener configurationListener : configurationListeners)
			enhancer.registerMethodInterceptor(configurationListener.getMethodInterceptor(pc));
		return enhancer;
	}

}
