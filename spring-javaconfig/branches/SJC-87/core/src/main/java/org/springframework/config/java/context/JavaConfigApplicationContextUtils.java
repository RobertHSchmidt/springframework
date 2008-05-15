package org.springframework.config.java.context;

import static java.lang.String.format;
import static org.springframework.config.java.core.Constants.INTERNAL_BEAN_FACTORY_NAME;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

class JavaConfigApplicationContextUtils {

	private JavaConfigApplicationContextUtils() { }

	public static DefaultJavaConfigBeanFactory getRequiredInternalBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) {
		if(!externalBeanFactory.containsSingleton(INTERNAL_BEAN_FACTORY_NAME))
			throw new IllegalStateException(
				format("No internal BeanFactory found: perhaps %s was not registered?",
					InternalBeanFactoryEstablishingBeanFactoryPostProcessor.class.getSimpleName()));
		return (DefaultJavaConfigBeanFactory) externalBeanFactory.getBean(INTERNAL_BEAN_FACTORY_NAME);
	}

}
