package org.springframework.config.java.core;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class BeanFactoryFactory {
	public static final String FACTORY_METHOD_NAME = "newBeanFactory";

	// TODO: hard-coding JCAC here is no good (creates a cyclic dep)... this needs to be
	// registered on the fly by the JavaConfigBeanDefinitionReader in use at runtime
	public static BeanFactory newBeanFactory(String className) throws ClassNotFoundException {
		return new JavaConfigApplicationContext(Class.forName(className));
	}
}
