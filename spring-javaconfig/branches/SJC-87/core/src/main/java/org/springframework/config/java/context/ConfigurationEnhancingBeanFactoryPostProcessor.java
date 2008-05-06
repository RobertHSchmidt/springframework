package org.springframework.config.java.context;

import static java.lang.String.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ResourceLoader;
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
public class ConfigurationEnhancingBeanFactoryPostProcessor implements BeanFactoryPostProcessor,
                                                                       ApplicationContextAware, InitializingBean {
	private static final Log log = LogFactory.getLog(ConfigurationEnhancingBeanFactoryPostProcessor.class);

	/**
	 * @see #afterPropertiesSet()
	 * @see #setConfigurationEnhancer(ConfigurationEnhancer)
	 */
	private ConfigurationEnhancer enhancer;

	private BeanFactory beanFactory;

	private ResourceLoader resourceLoader;

	public ConfigurationEnhancingBeanFactoryPostProcessor() { }

	public ConfigurationEnhancingBeanFactoryPostProcessor(ApplicationContext appContext) {
		setApplicationContext(appContext);
		setResourceLoader(appContext);
		afterPropertiesSet();
	}

	public void afterPropertiesSet() {
		if(beanFactory == null) throw new IllegalStateException("beanFactory was not set");
		if(resourceLoader == null) throw new IllegalStateException("resourceLoader was not set");

		// has an enhancer already been injected?
		if(enhancer != null) return;

		// no. provide a default;
		enhancer = new CglibConfigurationEnhancer(beanFactory, resourceLoader);
	}

	/** called by the enclosing BeanFactory during initialization */
	public void setApplicationContext(ApplicationContext appContext) {
		this.beanFactory = appContext;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * optional
	 * @see #afterPropertiesSet()
	 */
	public void setConfigurationEnhancer(ConfigurationEnhancer enhancer) {
		this.enhancer = enhancer;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("Post-processing " + beanFactory);
		Assert.notNull(beanFactory, "beanFactory is null. Perhaps setApplicationContext() was not called?");
		Assert.notNull(enhancer, "Enhancer is null. Perhaps afterPropertiesSet() was not called?");

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
