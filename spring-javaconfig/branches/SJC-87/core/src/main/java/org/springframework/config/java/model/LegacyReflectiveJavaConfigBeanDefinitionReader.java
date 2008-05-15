package org.springframework.config.java.model;


import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.context.DefaultJavaConfigBeanFactory;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * {@link JavaConfigBeanDefinitionReader} implementation primarily designed for refactoring
 * purposes while moving JavaConfig fram Java reflection-based class parsing to ASM-based class
 * parsing. "Legacy" because it simply bootstraps JavaConfig's {@link ConfigurationProcessor} in
 * order to call its
 * {@link ConfigurationProcessor#processConfigurationBean(String, Class) processConfigurationBean}
 * method.
 *
 * @see AsmJavaConfigBeanDefinitionReader
 * @see ReflectiveJavaConfigBeanDefinitionReader
 *
 * @author Chris Beams
 */
public class LegacyReflectiveJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader {

	private final ConfigurationProcessor processor;
	private final String configurationBeanName;

	public LegacyReflectiveJavaConfigBeanDefinitionReader(ConfigurationProcessor processor, String configurationBeanName) {
		super(new DefaultJavaConfigBeanFactory(new DefaultListableBeanFactory()));
		this.processor = processor;
		this.configurationBeanName = configurationBeanName;
	}

	public LegacyReflectiveJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry, String configurationBeanName) {
		this(new ConfigurationProcessor((ConfigurableListableBeanFactory)registry), configurationBeanName);
	}


	@Override
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		int initialBeanDefinitionCount = beanFactory.getBeanDefinitionCount();
		try {
			processor.processConfigurationBean(configurationBeanName,
					Class.forName(ClassUtils.convertResourcePathToClassName(((ClassPathResource)resource).getPath())));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return beanFactory.getBeanDefinitionCount() - initialBeanDefinitionCount;
	}

	/**
	 * Implemented for compatibility with template base class
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected void applyAdHocAspectsToModel(ConfigurationModel model) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Implemented for compatibility with template base class
	 * @throws UnsupportedOperationException
	 */
	@Override
	protected ConfigurationModel createConfigurationModel(Resource... configClassResources) {
		throw new UnsupportedOperationException();
	}
}