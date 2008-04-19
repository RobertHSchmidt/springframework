package org.springframework.config.java.context;

import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.config.java.model.ReflectingJavaConfigBeanDefinitionReader;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * re-implementing a simplified version of the context.  This will be swapped
 * in for the existing {@link LegacyJavaConfigApplicationContext} when feature-complete.
 */
public class JavaConfigApplicationContext extends AbstractRefreshableApplicationContext
                                          implements ConfigurableJavaConfigApplicationContext {

	// TODO: is the factory really necessary?
	private final ClassPathScanningCandidateComponentProvider scanner =
		new ClassPathScanningConfigurationProviderFactory().getProvider(this);

	// TODO: should be LinkedHashSet?
	private ArrayList<ClassPathResource> configClassResources = new ArrayList<ClassPathResource>();

	public JavaConfigApplicationContext() { }

	public JavaConfigApplicationContext(Class<?>... configClasses) {
		addConfigClasses(configClasses);
		refresh();
	}

	public JavaConfigApplicationContext(String... basePackages) {
		addBasePackages(basePackages);
		refresh();
	}

	@Override
	protected void prepareRefresh() {
		super.prepareRefresh();
		addBeanFactoryPostProcessor(new ConfigurationEnhancingBeanFactoryPostProcessor(this));
	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		BeanDefinitionReader reader = new ReflectingJavaConfigBeanDefinitionReader(beanFactory);
		for(ClassPathResource configClassResource : configClassResources)
			reader.loadBeanDefinitions(configClassResource);
	}

	public void addConfigClasses(Class<?>... classes) {
		for(Class<?> configClass : classes)
			addConfigClassAsResource(configClass.getName());
	}

	public void addBasePackages(String... basePackages) {
		for (String basePackage : basePackages)
			for(BeanDefinition beanDef : scanner.findCandidateComponents(basePackage))
				addConfigClassAsResource(beanDef.getBeanClassName());
	}

	private void addConfigClassAsResource(String fqClassName) {
		configClassResources.add(new ClassPathResource(convertClassNameToResourcePath(fqClassName)));
	}

	@Deprecated
	public void addConfigClass(Class<?> cls) { addConfigClasses(cls); }

	@Deprecated
	public void setBasePackages(String... basePackages) { addBasePackages(basePackages); }

	@Deprecated
	public void setConfigClasses(Class<?>... classes) { addConfigClasses(classes); }

	public <T> T getBean(Class<T> type) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
	}

	public <T> T getBean(Class<T> type, String beanName) {
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type);
	}

}