package org.springframework.config.java.process;

import java.util.HashMap;
import java.util.List;

import org.springframework.config.java.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;

class StandardBeanConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public boolean understands(Class<?> configurationClass) {
		return true;
		// !StandardBeanMethodProcessor.findBeanCreationMethods(configurationClass).isEmpty();
	}

	private final HashMap<Class<?>, List<String>> noArgMethodsSeen = new HashMap<Class<?>, List<String>>();

	public void handleEvent(Reactor reactor, MethodEvent event) {

		Bean beanAnnotation = AnnotationUtils.findAnnotation(event.method, Bean.class);
		if (beanAnnotation == null)
			return;

		/*

		if (!noArgMethodsSeen.containsKey(configurationClass))
			noArgMethodsSeen.put(configurationClass, new ArrayList<String>());

		// Determine bean name
		String beanName = configurationProcessor.getBeanNamingStrategy().getBeanName(m);

		if (!noArgMethodsSeen.get(configurationClass).contains(beanName)) {
			// If the bean already exists in the factory, don't emit
			// a bean definition. This may or may not be legal,
			// depending on whether the @Bean annotation allows
			// overriding
			if (configurationProcessor.owningBeanFactory.containsLocalBean(beanName)) {
				if (!beanAnnotation.allowOverriding()) {
					String message = format(
							"A bean named '%s' already exists. Consider using @Bean(allowOverriding=true)", beanName);
					throw new IllegalStateException(message);
				}
				// Don't emit a bean definition
				return 0;
			}
			noArgMethodsSeen.get(configurationClass).add(beanName);
			beanDefsGenerated += configurationProcessor.generateBeanDefinitionFromBeanCreationMethod(
					configurationProcessor.owningBeanFactory, configurationBeanName, configurationClass, beanName, m,
					beanAnnotation);
		}
		*/

	}
}
