package org.springframework.config.java.context;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.model.AspectClass;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.config.java.model.ReflectiveJavaConfigBeanDefinitionReader;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

public class ConfigurationClassParsingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {

	private static final Log log = LogFactory.getLog(ConfigurationClassParsingBeanFactoryPostProcessor.class);

	public void postProcessBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) throws BeansException {
		log.info("Parsing @Configuration classes within " + externalBeanFactory);
		DefaultJavaConfigBeanFactory internalBeanFactory = JavaConfigApplicationContextUtils.getRequiredInternalBeanFactory(externalBeanFactory);
		ArrayList<ClassPathResource> aspectClassResources = new ArrayList<ClassPathResource>();
		ArrayList<ClassPathResource> configClassResources = new ArrayList<ClassPathResource>();

		for(String beanName : externalBeanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = externalBeanFactory.getBeanDefinition(beanName);
			if(beanDef.isAbstract())
				continue;
			if(beanDef.hasAttribute(ConfigurationClass.BEAN_ATTR_NAME)) {
				String path = ClassUtils.convertClassNameToResourcePath(beanDef.getBeanClassName());
				configClassResources.add(new ClassPathResource(path));
			} else if(beanDef.hasAttribute(AspectClass.BEAN_ATTR_NAME)) {
				String path = ClassUtils.convertClassNameToResourcePath(beanDef.getBeanClassName());
				aspectClassResources.add(new ClassPathResource(path));
			}
		}

		ReflectiveJavaConfigBeanDefinitionReader reader = new ReflectiveJavaConfigBeanDefinitionReader(internalBeanFactory, aspectClassResources);
		reader.loadBeanDefinitions(configClassResources.toArray(new ClassPathResource[configClassResources.size()]));
	}

	public int getOrder() {
		return ORDER;
	}

	public static final int ORDER = InternalBeanFactoryEstablishingBeanFactoryPostProcessor.ORDER+1;

}
