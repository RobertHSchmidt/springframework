package org.springframework.config.java.process;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.config.java.internal.util.ConfigurationUtils;

/**
 * Detects the presence of any {@link Configuration @Configuration} bean definitions and annotates them with
 * {@link BeanMetadataAttribute bean metadata}.  For use only when bootstrapping JavaConfig via XML.
 *
 * @author Chris Beams
 */
class ConfigurationBeanDefinitionDecoratingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for(String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			if(isConfigClass(beanDef))
				addConfigClassMetadata((BeanMetadataAttributeAccessor) beanDef);
		}
	}

	private void addConfigClassMetadata(BeanMetadataAttributeAccessor beanDef) {
		// TODO: {DRY}
		beanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
	}

	/* this is the eventual implementation
	private boolean isConfigClass(BeanDefinition beanDef) {
		BeanDefinition beanDefinition = beanDef;
		String className = beanDefinition.getBeanClassName();
		MetadataReader reader;

		try {
			reader = new SimpleMetadataReaderFactory().getMetadataReader(className);
		}
		catch (IOException e) {
			throw new BeanCreationException("problem reading class " + className, e);
		}

		return reader.getAnnotationMetadata().hasAnnotation(Configuration.class.getName());
	}
	*/

	/* but this one should be used in the meantime while transitioning */
	private boolean isConfigClass(BeanDefinition beanDef) {
		try {
			String className = beanDef.getBeanClassName();
			if(className == null)
				return false;

			return ConfigurationUtils.isConfigurationClass(Class.forName(className));
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

}
