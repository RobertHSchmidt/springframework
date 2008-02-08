package org.springframework.config.java.process;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.core.BeanNameTrackingDefaultListableBeanFactory;
import org.springframework.config.java.core.Constants;
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
		ProcessingContext pc = getProcessingContext();
		Method m = event.method;
		Bean beanAnnotation = AnnotationUtils.findAnnotation(m, Bean.class);
		if (beanAnnotation == null)
			return;

		Class<?> configurationClass = event.clazz;
		String configurationBeanName = event.configurationBeanName;

		BeanNamingStrategy beanNamingStrategy = pc.beanNamingStrategy;
		ConfigurableListableBeanFactory owningBeanFactory = pc.owningBeanFactory;
		BeanNameTrackingDefaultListableBeanFactory childFactory = pc.childFactory;

		String beanName = beanNamingStrategy.getBeanName(m);

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

		ProcessUtils.validateBeanCreationMethod(m);

		// Create a bean definition from the method
		RootBeanDefinition rbd = new RootBeanDefinition(m.getReturnType());

		rbd.setFactoryMethodName(m.getName());
		rbd.setFactoryBeanName(configurationBeanName);
		// tag the bean definition
		rbd.setAttribute(Constants.JAVA_CONFIG_PKG, Boolean.TRUE);

		Configuration config = configurationClass.getAnnotation(Configuration.class);

		ProcessUtils.copyAttributes(beanName, beanAnnotation, config, rbd, owningBeanFactory);

		// create description string
		StringBuilder builder = new StringBuilder("Bean creation method ");
		builder.append(m.getName());
		builder.append(" in class ");
		builder.append(m.getDeclaringClass().getName());
		rbd.setResourceDescription(builder.toString());

		// create a beanDefinitionRegistration for the current bean
		// definition/name pair
		BeanDefinitionRegistration beanDefinitionRegistration = new BeanDefinitionRegistration(rbd, beanName);
		beanDefinitionRegistration.hide = !Modifier.isPublic(m.getModifiers());

		BeanMethodEvent beanMethodEvent = new BeanMethodEvent(this, configurationClass, m, beanAnnotation,
				beanDefinitionRegistration);

		reactor.sourceBeanMethodEvent(beanMethodEvent);

		// allow registration bypass
		if (beanDefinitionRegistration.rbd == null) {
			return;
		}

		if (beanDefinitionRegistration.hide) {
			childFactory.registerBeanDefinition(beanDefinitionRegistration.name, beanDefinitionRegistration.rbd);
		}
		else {
			((BeanDefinitionRegistry) owningBeanFactory).registerBeanDefinition(beanDefinitionRegistration.name,
					beanDefinitionRegistration.rbd);
		}

		pc.beanDefsGenerated++;
	}
}
