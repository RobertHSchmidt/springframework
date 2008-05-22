package org.springframework.config.java.model;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.config.java.context.JavaConfigBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Not safe for use with tooling (Spring IDE); being developed as a bridge during refactoring to ASM.
 *
 * @author Chris Beams
 */
public class ReflectiveJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader implements JavaConfigBeanDefinitionReader {


	private final JavaConfigAspectRegistry aspectRegistry;

	public ReflectiveJavaConfigBeanDefinitionReader(JavaConfigBeanFactory registry,
			List<ClassPathResource> aspectClassResources) {
		super(registry, aspectClassResources);
		this.aspectRegistry = new JavaConfigAspectRegistry(beanFactory);
	}


	/**
	 * Create an abstract {@link ConfigurationModel} from a set of {@link Configuration @Configuration}
	 * classes.  Classes are {@link Resource} objects rather than class literals in order to interoperate
	 * with tooling (Spring IDE) effectively.
	 * @param configClassResources set of Configuration class resources
	 * @return configuration model representing logical structure of configuration metadata within
	 * those classes
	 */
	@Override
	protected ConfigurationModel createConfigurationModel(Resource... configClassResources) {
		ConfigurationModel model = new ConfigurationModel();
		ReflectiveConfigurationParser parser = new ReflectiveConfigurationParser(model);
		for(Resource configClassResource : configClassResources)
			parser.parse(loadClassFromResource(configClassResource));
		return model;
	}


	@Override
	protected void applyAdHocAspectsToModel(ConfigurationModel model) {
		for(ClassPathResource aspectClassResource : this.getAspectClassResources()) {
			Class<?> aspectClass = loadClassFromResource(aspectClassResource);
			Aspect metadata = aspectClass.getAnnotation(Aspect.class); // may be null
			model.add(new AspectClass(aspectClass.getName(), metadata));
		}
	}

	@Override
	protected void registerAspectsFromModel(ConfigurationModel model) {
		if(log.isInfoEnabled())
			log.info("Registering aspects from " + model);

		ArrayList<Class<?>> aspectClasses = new ArrayList<Class<?>>();
		for(AspectClass aspectClass : model.getAspectClasses()) {
			try {
				aspectClasses.add(Class.forName(aspectClass.getName()));
			}
			catch (ClassNotFoundException ex) { throw new RuntimeException(ex); }
		}
		Class<?>[] atAspectClasses = aspectClasses.toArray(new Class<?>[aspectClasses.size()]);

		aspectRegistry.registerAspects(atAspectClasses);
	}

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
