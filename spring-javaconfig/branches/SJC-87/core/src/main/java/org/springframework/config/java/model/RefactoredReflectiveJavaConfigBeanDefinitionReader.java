package org.springframework.config.java.model;

import static java.lang.String.format;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

public class RefactoredReflectiveJavaConfigBeanDefinitionReader extends AbstractJavaConfigBeanDefinitionReader implements JavaConfigBeanDefinitionReader {

	public RefactoredReflectiveJavaConfigBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}

	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		int initialBeanDefinitionCount = this.getRegistry().getBeanDefinitionCount();
		final Class<?> configurationClass;

		// load resource as class
		try {
			// TODO: what if the resource is not a ClassPathResource?
			configurationClass = Class.forName(ClassUtils.convertResourcePathToClassName(((ClassPathResource)resource).getPath()));
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(format("could not load resource as class [%s]", resource), ex);
		}

		// initialize a new model
		JavaConfigurationModel model = new JavaConfigurationModel();

		// populate model reflectively
		ReflectiveJavaConfigurationModelPopulator modelPopulator =
			new ReflectiveJavaConfigurationModelPopulator(model);
		modelPopulator.addToModel(configurationClass);

		// render model as BeanDefinitions within this.registry
		BeanDefinitionJavaConfigurationModelRenderer modelRenderer =
			new BeanDefinitionJavaConfigurationModelRenderer(this.getRegistry());
		modelRenderer.renderModel(model);

		return this.getRegistry().getBeanDefinitionCount() - initialBeanDefinitionCount;
	}

}
