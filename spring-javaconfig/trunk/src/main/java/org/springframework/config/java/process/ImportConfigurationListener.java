package org.springframework.config.java.process;

import org.springframework.config.java.annotation.Import;

class ImportConfigurationListener extends ConfigurationListenerSupport {

	@Override
	public boolean understands(Class<?> configurationClass) {
		// TODO: does Import work with inheritance?
		return configurationClass.isAnnotationPresent(Import.class);
	}

	@Override
	public int configurationClass(ConfigurationProcessor configurationProcessor, String configurerBeanName,
			Class<?> configurationClass) {
		int nBeanDefsGenerated = 0;
		Import importAnnotation = configurationClass.getAnnotation(Import.class);
		Class<?>[] configurationClassesToImport = ConfigurationProcessor.reverse(importAnnotation.value());
		for (Class<?> configurationClassToImport : configurationClassesToImport) {
			// duplicate check - process only if we've never encountered before
			if (!configurationProcessor.owningBeanFactory.containsBeanDefinition(configurationClassToImport.getName()))
				nBeanDefsGenerated += configurationProcessor.processClass(configurationClassToImport);
		}
		return nBeanDefsGenerated;
	}

}
