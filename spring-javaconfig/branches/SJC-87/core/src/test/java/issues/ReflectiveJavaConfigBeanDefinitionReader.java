package issues;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.process.ConfigurationProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * {@link JavaConfigBeanDefinitionReader} implementation primarily designed for refactoring
 * purposes while moving JavaConfig fram Java reflection-based class parsing to ASM-based class
 * parsing.
 * 
 * @see AsmJavaConfigBeanDefinitionReader
 *
 * @author Chris Beams
 */
public class ReflectiveJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader {

	private final ConfigurationProcessor processor;
	private final String configurationBeanName;

	public ReflectiveJavaConfigBeanDefinitionReader(ConfigurationProcessor processor, String configurationBeanName) {
		super(new DefaultListableBeanFactory());
		this.processor = processor;
		this.configurationBeanName = configurationBeanName;
	}

	public ReflectiveJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry, String configurationBeanName) {
		this(new ConfigurationProcessor((ConfigurableListableBeanFactory)registry), configurationBeanName);
	}

	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		int initialBeanDefinitionCount = this.getRegistry().getBeanDefinitionCount();
		try {
			processor.processConfigurationBean(configurationBeanName,
					Class.forName(ClassUtils.convertResourcePathToClassName(((ClassPathResource)resource).getPath())));
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return this.getRegistry().getBeanDefinitionCount() - initialBeanDefinitionCount;
	}
}