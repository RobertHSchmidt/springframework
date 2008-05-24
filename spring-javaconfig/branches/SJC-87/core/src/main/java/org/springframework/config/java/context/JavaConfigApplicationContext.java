package org.springframework.config.java.context;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.model.AspectClass;
import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

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
	private final ArrayList<Class<?>> configClasses = new ArrayList<Class<?>>();

	private final ArrayList<Class<?>> aspectClasses = new ArrayList<Class<?>>();

	private final ArrayList<String> basePackages = new ArrayList<String>();

	private BeanNamingStrategy beanNamingStrategy = new MethodNameStrategy();

	/** context is configurable until refresh() is called */
	private boolean openForConfiguration = true;

	public JavaConfigApplicationContext() { }

	public JavaConfigApplicationContext(Class<?>... configClasses) {
		addConfigClasses(configClasses);
		refresh();
	}

	public JavaConfigApplicationContext(String... basePackages) {
		addBasePackages(basePackages);
		refresh();
	}

	public JavaConfigApplicationContext(Class<?>[] classes, String[] packages) {
		addConfigClasses(classes);
		addBasePackages(packages);
		refresh();
	}

	/**
	 * Create a new context with <var>parent</var> as the parent.  Requires an explicit call to refresh().
	 *
	 * @param parent
	 */
	public JavaConfigApplicationContext(JavaConfigApplicationContext parent) {
		super(parent);
	}

	public JavaConfigApplicationContext(ApplicationContext parent, Class<?>... classes) {
		super(parent);
		addConfigClasses(classes);
		refresh();
	}

	@Override
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		InternalBeanFactoryEstablishingBeanFactoryPostProcessor iBPP = new InternalBeanFactoryEstablishingBeanFactoryPostProcessor(this);
		if(this.getBeanNamingStrategy() != null)
			iBPP.setBeanNamingStrategy(this.getBeanNamingStrategy());
		iBPP.postProcessBeanFactory(beanFactory);
		new ConfigurationClassParsingBeanFactoryPostProcessor().postProcessBeanFactory(beanFactory);
		new ConfigurationEnhancingBeanFactoryPostProcessor().postProcessBeanFactory(beanFactory);
		super.invokeBeanFactoryPostProcessors(beanFactory);
	}

	@Override
	protected void finishRefresh() {
		super.finishRefresh();
		openForConfiguration = false;
	}

	/**
	 * Loads any specified {@link Configuration @Configuration} classes and {@link Aspect @Aspect} classes
	 * as bean definitions within this context's BeanFactory for later processing by {@link ConfigurationPostProcessor}
	 * @see #JavaConfigApplicationContext(Class...)
	 * @see #addConfigClasses(Class...)
	 * @see #addAspectClasses(Class...)
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
		for(Class<?> configClass : configClasses)
			loadBeanDefinitionForConfigurationClass(beanFactory, configClass);

		for(String basePackage : basePackages)
			loadBeanDefinitionsForBasePackage(beanFactory, basePackage);

		for(Class<?> aspectClass : aspectClasses)
			loadBeanDefinitionForAspectClass(beanFactory, aspectClass);
	}

	private void loadBeanDefinitionForConfigurationClass(DefaultListableBeanFactory beanFactory, Class<?> configClass) {
		String configBeanName = configClass.getName(); // TODO: {naming strategy} should end in # mark?
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configBeanName);
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
		beanFactory.registerBeanDefinition(configBeanName, configBeanDef);
	}

	private void loadBeanDefinitionsForBasePackage(DefaultListableBeanFactory beanFactory, String basePackage) {
		for(BeanDefinition beanDef : scanner.findCandidateComponents(basePackage)) {
			ScannedGenericBeanDefinition configBeanDef = (ScannedGenericBeanDefinition) beanDef; // TODO: unfortunate cast
			String configBeanName = configBeanDef.getBeanClassName(); // TODO: {naming strategy}
			configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));
			beanFactory.registerBeanDefinition(configBeanName, configBeanDef);
		}
	}

	private void loadBeanDefinitionForAspectClass(DefaultListableBeanFactory beanFactory, Class<?> aspectClass) {
		String aspectBeanName = aspectClass.getName(); // TODO: {naming strategy} should end in # mark?
		RootBeanDefinition aspectBeanDef = new RootBeanDefinition();
		aspectBeanDef.setBeanClassName(aspectBeanName);
		aspectBeanDef.addMetadataAttribute(new BeanMetadataAttribute(AspectClass.BEAN_ATTR_NAME, true));
		beanFactory.registerBeanDefinition(aspectBeanName, aspectBeanDef);
	}

	@Override
	public void setParent(ApplicationContext parent) {
		assertOpenForConfiguration("setParent");
		super.setParent(parent);
	}

	public void setBeanNamingStrategy(BeanNamingStrategy namingStrategy) {
		this.beanNamingStrategy = namingStrategy;
	}

	public BeanNamingStrategy getBeanNamingStrategy() {
		return beanNamingStrategy;
	}

	public void addConfigClasses(Class<?>... classes) {
		assertOpenForConfiguration("addConfigClasses");
		for(Class<?> configClass : classes)
			configClasses.add(configClass);
	}

	public void addBasePackages(String... packages) {
		assertOpenForConfiguration("addBasePackages");
		for (String basePackage : packages)
			basePackages.add(basePackage);
	}

	public void addAspectClasses(Class<?>... classes) {
		assertOpenForConfiguration("addAspectClasses");
		for (Class<?> aspectClass : classes)
			aspectClasses.add(aspectClass);
	}

	private void assertOpenForConfiguration(String attemptedMethod) {
		if(!openForConfiguration)
    		throw new IllegalStateException(format(
    				"context is closed for configuration. %s() must be called " +
    				"before refresh(), consider using the no-arg constructor for %s",
    				attemptedMethod, this.getClass().getSimpleName()
    			));
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
		return TypeSafeBeanFactoryUtils.getBean(this.getBeanFactory(), type, beanName);
	}

}
