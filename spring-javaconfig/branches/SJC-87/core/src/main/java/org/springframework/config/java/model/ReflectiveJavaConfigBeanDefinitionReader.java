package org.springframework.config.java.model;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

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
public class ReflectiveJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader implements JavaConfigBeanDefinitionReader {

	private final List<ClassPathResource> aspectClassResources;
	private static final String cmapBeanName = ConfigurationModelAspectProcessor.class.getName();
	private BeanDefinitionRegisteringConfigurationModelRenderer modelRenderer;

	public ReflectiveJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, new ArrayList<ClassPathResource>());
	}


	public ReflectiveJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry, ArrayList<ClassPathResource> aspectClassResources) {
		super(registry);
		this.aspectClassResources = aspectClassResources;
		// register a bean definition for a factory that can be used when rendering declaring classes
		registerBeanFactoryFactory(registry);

		if(!((SingletonBeanRegistry)registry).containsSingleton(cmapBeanName))
			((SingletonBeanRegistry)registry).registerSingleton(cmapBeanName, new ConfigurationModelAspectProcessor());

		modelRenderer = new BeanDefinitionRegisteringConfigurationModelRenderer(registry);
	}

	@Override
	public int loadBeanDefinitions(Resource[] configClassResources) throws BeanDefinitionStoreException {
		ConfigurationModel model = createConfigurationModel(configClassResources);

		applyAdHocAspectsToModel(model);

		validateModel(model);

		registerAspectsFromModel(model);

		return loadBeanDefinitionsFromModel(model);
	}

	public int loadBeanDefinitions(Resource configClassResource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new Resource[] { configClassResource } );
	}


	private void registerAspectsFromModel(ConfigurationModel model) {
		ConfigurationModelAspectProcessor cmap =
			(ConfigurationModelAspectProcessor) ((BeanFactory)getRegistry()).getBean(cmapBeanName);
		cmap.processAnyAspects(model, (BeanFactory) getRegistry());
	}


	/**
	 * @param model
	 * @return number of bean definitions registered
	 */
	private int loadBeanDefinitionsFromModel(ConfigurationModel model) {
		return modelRenderer.render(model);
	}


	private void validateModel(ConfigurationModel model) {
		model.assertIsValid();
	}

	/**
	 * Create an abstract {@link ConfigurationModel} from a set of {@link Configuration @Configuration}
	 * classes.  Classes are {@link Resource} objects rather than class literals in order to interoperate
	 * with tooling (Spring IDE) effectively.
	 * @param configClassResources set of Configuration class resources
	 * @return configuration model representing logical structure of configuration metadata within
	 * those classes
	 */
	private ConfigurationModel createConfigurationModel(Resource... configClassResources) {
		ConfigurationModel model = new ConfigurationModel();
		ReflectiveConfigurationParser parser = new ReflectiveConfigurationParser(model);
		for(Resource configClassResource : configClassResources)
			parser.parse(loadClassFromResource(configClassResource));
		return model;
	}

	private void applyAdHocAspectsToModel(ConfigurationModel model) {
		// add any ad-hoc aspects to the model
		for(ClassPathResource aspectClassResource : aspectClassResources) {
			Class<?> aspectClass = loadClassFromResource(aspectClassResource);
			Aspect metadata = aspectClass.getAnnotation(Aspect.class); // may be null
			model.add(new AspectClass(aspectClass.getName(), metadata));
		}
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
