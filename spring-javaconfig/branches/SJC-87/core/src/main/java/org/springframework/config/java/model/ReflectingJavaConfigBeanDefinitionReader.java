package org.springframework.config.java.model;

import static java.lang.String.format;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.core.BeanFactoryFactory;
import org.springframework.config.java.core.Constants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Not safe for use with tooling (Spring IDE); being developed as a bridge during refactoring to ASM.
 *
 * @author Chris Beams
 */
public class ReflectingJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader implements JavaConfigBeanDefinitionReader {

	public ReflectingJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}

	public int loadBeanDefinitions(Resource configClass) throws BeanDefinitionStoreException {
		BeanDefinitionRegistry registry = this.getRegistry();

		int initialBeanDefinitionCount = registry.getBeanDefinitionCount();

		// initialize a new model
		ConfigurationModel model = new ConfigurationModel();

		// parse the class and populate the model using reflection
		new ReflectingConfigurationParser(model).parse(loadClassFromResource(configClass));

		// is the model valid? TODO: perhaps should go into the parse() method above
		model.assertIsValid();

		// register a bean definition for a factory that can be used when rendering declaring classes
		registerBeanFactoryFactory(registry);

		// render model by creating BeanDefinitions based on the model and registering them within registry
		new BeanDefinitionRegisteringConfigurationModelRenderer(registry).render(model);

		// return the total number of bean definitions registered
		return registry.getBeanDefinitionCount() - initialBeanDefinitionCount;
	}

	// TODO: document this extensively.  the declaring class logic is quite complex, potentially confusing right now.
	private void registerBeanFactoryFactory(BeanDefinitionRegistry registry) {
		RootBeanDefinition bff = new RootBeanDefinition();
		String factoryName = BeanFactoryFactory.class.getName();
		bff.setBeanClassName(factoryName);
		bff.setFactoryMethodName(BeanFactoryFactory.FACTORY_METHOD_NAME);
		bff.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		bff.addMetadataAttribute(new BeanMetadataAttribute(Constants.JAVA_CONFIG_IGNORE, true));

		registry.registerBeanDefinition(factoryName, bff);
	}

	// TODO: probably belongs in a more general-purose util class
	private Class<?> loadClassFromResource(Resource configClass) throws BeanDefinitionStoreException {
		// load resource as class
		try {
			// TODO: what if the resource is not a ClassPathResource?
			return Class.forName(ClassUtils.convertResourcePathToClassName(((ClassPathResource)configClass).getPath()));
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(format("could not load class from resource [%s]", configClass), ex);
		}
	}

}
