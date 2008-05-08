package org.springframework.config.java.context;

import static java.lang.String.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

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
public class ConfigurationEnhancingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {
	private static final Log log = LogFactory.getLog(ConfigurationEnhancingBeanFactoryPostProcessor.class);

	private ConfigurationEnhancer enhancer;
	private ConfigurableListableBeanFactory internalBeanFactory;
	private ApplicationContext context;

	public void setConfigurationEnhancer(ConfigurationEnhancer enhancer) {
		this.enhancer = enhancer;
	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Assert.isInstanceOf(ConfigurableApplicationContext.class, context);
		// TODO: Assert.isInstanceOf(JavaConfigApplicationContext.class, context);
		this.context = context;
	}

	protected ConfigurationEnhancer initConfigurationEnhancer(ConfigurableListableBeanFactory externalBeanFactory) {
		if(context instanceof JavaConfigApplicationContext)
			internalBeanFactory = ((JavaConfigApplicationContext) context).getInternalBeanFactory();
		else {
			// TODO: replace with JavaConfigBeanFactory / InformationHidingBeanFactory
			internalBeanFactory = new DefaultListableBeanFactory(externalBeanFactory) {
				@Override
				public boolean isCurrentlyInCreation(String beanName) {
					if(super.isCurrentlyInCreation(beanName))
						return true;

					ConfigurableBeanFactory bf = (ConfigurableBeanFactory) this.getParentBeanFactory();
					while(bf != null) {
						if(bf.isCurrentlyInCreation(beanName))
							return true;
						bf = (ConfigurableBeanFactory) bf.getParentBeanFactory();
					}
					return false;
				}
			};
		}
		return new CglibConfigurationEnhancer(internalBeanFactory);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("Post-processing " + beanFactory);

		if(enhancer == null)
			enhancer = initConfigurationEnhancer(beanFactory);

		int configClassesEnhanced = 0;

		for(String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);

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
			log.warn("Found no @Configuration class BeanDefinitions within " + beanFactory);
	}

}
