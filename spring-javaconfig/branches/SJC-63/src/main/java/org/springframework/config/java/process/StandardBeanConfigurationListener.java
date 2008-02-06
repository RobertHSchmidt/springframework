package org.springframework.config.java.process;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.core.StandardBeanMethodProcessor;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.core.annotation.AnnotationUtils;

class StandardBeanConfigurationListener extends ConfigurationListenerSupport {
	@Override
	public boolean understands(Class<?> configurationClass) {
		return !(StandardBeanMethodProcessor.findBeanCreationMethods(configurationClass).isEmpty());
	}

	private final HashMap<Class<?>, List<String>> noArgMethodsSeen = new HashMap<Class<?>, List<String>>();

	@Override
	public void handleEvent(Reactor reactor, MethodEvent event) {
		Method m = event.method;
		Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
		if (beanAnnotation == null)
			return;

		Class<?> configurationClass = event.clazz;
		String configurationBeanName = event.configurationBeanName;
		BeanNamingStrategy beanNamingStrategy = event.beanNamingStrategy;
		ConfigurableListableBeanFactory owningBeanFactory = event.owningBeanFactory;
		String beanName = beanNamingStrategy.getBeanName(m);
		ConfigurationProcessor cp = (ConfigurationProcessor) reactor;

		if (!noArgMethodsSeen.containsKey(configurationClass))
			noArgMethodsSeen.put(configurationClass, new ArrayList<String>());

		if (noArgMethodsSeen.get(configurationClass).contains(beanName))
			return;

		// If the bean already exists in the factory, don't emit
		// a bean definition. This may or may not be legal,
		// depending on whether the @Bean annotation allows
		// overriding
		if (owningBeanFactory.containsLocalBean(beanName)) {
			if (!beanAnnotation.allowOverriding()) {
				String message = String.format(
						"A bean named '%s' already exists. Consider using @Bean(allowOverriding=true)", beanName);
				throw new IllegalStateException(message);
			}
			// Don't emit a bean definition
			return;
		}
		noArgMethodsSeen.get(configurationClass).add(beanName);

		cp.generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory, configurationBeanName, configurationClass,
				beanName, m, beanAnnotation);
	}
}
