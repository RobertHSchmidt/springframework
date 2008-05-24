package org.springframework.config.java.processinternal;

import static java.lang.String.format;
import static org.springframework.config.java.util.Constants.INTERNAL_BEAN_FACTORY_NAME;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.factory.JavaConfigBeanFactory;

class JavaConfigApplicationContextUtils {

	private JavaConfigApplicationContextUtils() { }

	public static JavaConfigBeanFactory getRequiredInternalBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) {
		if(!externalBeanFactory.containsSingleton(INTERNAL_BEAN_FACTORY_NAME))
			throw new IllegalStateException(
				format("No internal BeanFactory found: perhaps %s was not registered?",
					InternalBeanFactoryEstablishingBeanFactoryPostProcessor.class.getSimpleName()));
		return (JavaConfigBeanFactory) externalBeanFactory.getBean(INTERNAL_BEAN_FACTORY_NAME);
	}

}
