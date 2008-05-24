package org.springframework.config.java.internal.process;

import static java.lang.String.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.internal.enhancement.CglibConfigurationEnhancer;
import org.springframework.config.java.internal.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.internal.factory.JavaConfigBeanFactory;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.core.PriorityOrdered;

/**
 * Post-processes a BeanFactory in search of Configuration class BeanDefinitions;
 * any candidates are then enhanced by a {@link ConfigurationEnhancer}.  Candidate
 * status is determined by BeanDefinition attribute metadata.
 *
 * @see ConfigurationClass#BEAN_ATTR_NAME
 * @see ConfigurationEnhancer
 * @see BeanFactoryPostProcessor
 *
 * @author Chris Beams
 */
public class ConfigurationEnhancingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, PriorityOrdered {
	private static final Log log = LogFactory.getLog(ConfigurationEnhancingBeanFactoryPostProcessor.class);

	private ConfigurationEnhancer enhancer;

	/** optional for unit-testing purposes */
	public void setConfigurationEnhancer(ConfigurationEnhancer enhancer) { this.enhancer = enhancer; }


	public void postProcessBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) throws BeansException {
		log.info("Post-processing " + externalBeanFactory);
		JavaConfigBeanFactory internalBeanFactory = JavaConfigApplicationContextUtils.getRequiredInternalBeanFactory(externalBeanFactory);
		//Assert.isTrue(internalBeanFactory.getParentBeanFactory() == externalBeanFactory);

		if(enhancer == null)
			enhancer = new CglibConfigurationEnhancer(internalBeanFactory);

		int configClassesEnhanced = 0;

		for(String beanName : externalBeanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = externalBeanFactory.getBeanDefinition(beanName);

			// is the beanDef marked as representing a configuration class?
			if(!beanDef.hasAttribute(ConfigurationClass.BEAN_ATTR_NAME))
				continue;

			String configClassName = beanDef.getBeanClassName();

			String enhancedClassName = enhancer.enhance(configClassName);

			if(log.isDebugEnabled())
				log.debug(format("Replacing bean definition class name [%s] with enhanced class name [%s]",
				                 configClassName, enhancedClassName));
			beanDef.setBeanClassName(enhancedClassName);

			configClassesEnhanced++;
		}

		if(configClassesEnhanced == 0)
			log.warn("Found no @Configuration class BeanDefinitions within " + internalBeanFactory);
	}

	public int getOrder() {
		return ORDER;
	}

	public static final int ORDER = ConfigurationClassParsingBeanFactoryPostProcessor.ORDER+1;
}
