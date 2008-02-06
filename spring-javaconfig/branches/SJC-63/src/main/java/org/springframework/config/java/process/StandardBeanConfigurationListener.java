package org.springframework.config.java.process;

import java.lang.reflect.Method;
import java.util.Set;

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

	/*
	private final HashMap<Class<?>, List<String>> noArgMethodsSeen = new HashMap<Class<?>, List<String>>();
	*/

	@Override
	public void handleEvent(Reactor reactor, MethodEvent event) {
		Method m = event.method;
		Class<?> configurationClass = event.clazz;
		String configurationBeanName = event.configurationBeanName;
		BeanNamingStrategy beanNamingStrategy = event.beanNamingStrategy;
		ConfigurableListableBeanFactory owningBeanFactory = event.owningBeanFactory;
		Set<String> noArgMethodsSeen = event.noArgMethodsSeen;
		Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
		String beanName = beanNamingStrategy.getBeanName(m);
		ConfigurationProcessor cp = (ConfigurationProcessor) reactor;

		if (beanAnnotation != null) {
			if (!noArgMethodsSeen.contains(beanName)) {
				// If the bean already exists in the factory, don't emit
				// a bean definition. This may or may not be legal,
				// depending on whether the @Bean annotation allows
				// overriding
				if (owningBeanFactory.containsLocalBean(beanName)) {
					if (!beanAnnotation.allowOverriding()) {
						String message = String.format(
								"A bean named '%s' already exists. Consider using @Bean(allowOverriding=true)",
								beanName);
						throw new IllegalStateException(message);
					}
					// Don't emit a bean definition
					return;
				}
				noArgMethodsSeen.add(beanName);
				cp.generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory, configurationBeanName,
						configurationClass, beanName, m, beanAnnotation);
			}
		}
	}

	public void foo(Reactor reactor, MethodEvent event) {
		Method m = event.method;
		Class<?> configurationClass = event.clazz;
		String configurationBeanName = configurationClass.getName();
		BeanNamingStrategy beanNamingStrategy = event.beanNamingStrategy;
		ConfigurableListableBeanFactory owningBeanFactory = event.owningBeanFactory;
		Set<String> noArgMethodsSeen = event.noArgMethodsSeen;

		Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
		if (beanAnnotation == null)
			return;

		/*
		if (!noArgMethodsSeen.containsKey(configurationClass))
			noArgMethodsSeen.put(configurationClass, new ArrayList<String>());
		*/

		// Determine bean name
		String beanName = beanNamingStrategy.getBeanName(m);

		/*
		if (!noArgMethodsSeen.get(configurationClass).contains(beanName)) {
		*/
		if (!noArgMethodsSeen.contains(beanName)) {
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
			/*
			noArgMethodsSeen.get(configurationClass).add(beanName);
			*/
			noArgMethodsSeen.add(beanName);
			((ConfigurationProcessor) reactor).generateBeanDefinitionFromBeanCreationMethod(owningBeanFactory,
					configurationBeanName, configurationClass, beanName, m, beanAnnotation);
		}
	}
}
