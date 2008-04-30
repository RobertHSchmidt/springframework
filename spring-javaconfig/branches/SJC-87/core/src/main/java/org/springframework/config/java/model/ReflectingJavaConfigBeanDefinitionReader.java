package org.springframework.config.java.model;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
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

	private final List<Entry<ClassPathResource, Aspect>> aspectClassResources;

	public ReflectingJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, new ArrayList<Entry<ClassPathResource, Aspect>>());
	}

	public ReflectingJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry,
			List<Entry<ClassPathResource, Aspect>> aspectClassResources) {
		super(registry);
		this.aspectClassResources = aspectClassResources;
	}

	public int loadBeanDefinitions(Resource configClass) throws BeanDefinitionStoreException {
		BeanDefinitionRegistry registry = this.getRegistry();

		int initialBeanDefinitionCount = registry.getBeanDefinitionCount();

		// initialize a new model
		ConfigurationModel model = new ConfigurationModel();

		// add any ad-hoc aspects to the model
		for(Entry<ClassPathResource, Aspect> entry : aspectClassResources) {
			model.add(new AspectClass(ClassUtils.convertResourcePathToClassName(entry.getKey().getPath()), entry.getValue()));
		}

		// parse the class and populate the model using reflection
		new ReflectingConfigurationParser(model).parse(loadClassFromResource(configClass));

		// is the model valid? TODO: perhaps should go into the parse() method above
		model.assertIsValid();

		// register a bean definition for a factory that can be used when rendering declaring classes
		registerBeanFactoryFactory(registry);

		// render model by creating BeanDefinitions based on the model and registering them within registry
		new BeanDefinitionRegisteringConfigurationModelRenderer(registry).render(model);

		String cmapBeanName = ConfigurationModelAspectProcessor.class.getName();

		if(!((SingletonBeanRegistry)registry).containsSingleton(cmapBeanName))
			((SingletonBeanRegistry)registry).registerSingleton(cmapBeanName, new ConfigurationModelAspectProcessor());

		ConfigurationModelAspectProcessor cmap =
			(ConfigurationModelAspectProcessor) ((BeanFactory)registry).getBean(cmapBeanName);

		cmap.processAnyAspects(model, (BeanFactory) registry);

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
