package org.springframework.config.java.core;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.context.JavaConfigApplicationContext;

public class BeanFactoryFactory {
	private static final String FACTORY_METHOD_NAME = "newBeanFactory";
	private static final String BEAN_CLASS_NAME = BeanFactoryFactory.class.getName();
	public static final String BEAN_NAME = BEAN_CLASS_NAME;

	// TODO: hard-coding JCAC here is no good (creates a cyclic dep)... this needs to be
	// registered on the fly by the JavaConfigBeanDefinitionReader in use at runtime
	public static BeanFactory newBeanFactory(String className) throws ClassNotFoundException {
		return new JavaConfigApplicationContext(Class.forName(className));
	}

	public static BeanDefinition createBeanDefinition() {
		RootBeanDefinition bff = new RootBeanDefinition();
		bff.setBeanClassName(BEAN_CLASS_NAME);
		bff.setFactoryMethodName(BeanFactoryFactory.FACTORY_METHOD_NAME);
		bff.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		bff.addMetadataAttribute(new BeanMetadataAttribute(Constants.JAVA_CONFIG_IGNORE, true));
		return bff;
	}
}
