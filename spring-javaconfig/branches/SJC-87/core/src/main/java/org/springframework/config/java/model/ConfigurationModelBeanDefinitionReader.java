package org.springframework.config.java.model;


import static java.lang.String.format;

import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Primary;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.core.BeanFactoryFactory;
import org.springframework.config.java.core.ScopedProxyMethodProcessor;
import org.springframework.config.java.type.Type;
import org.springframework.config.java.valuesource.MessageSourceValueSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * Renders a given {@link ConfigurationModel} as bean definitions to be
 * registered on-the-fly with a given {@link BeanDefinitionRegistry}.
 * Modeled after the {@link BeanDefinitionReader} hierarchy, but could not extend
 * directly as {@link ConfigurationModel} is not a {@link Resource}
 *
 * @author Chris Beams
 */
public class ConfigurationModelBeanDefinitionReader {

	private static final Log logger = LogFactory.getLog(ConfigurationModelBeanDefinitionReader.class);

	private final BeanDefinitionRegistry registry;

	public ConfigurationModelBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	private BeanDefinitionRegistry getInternalRegistry() {
		return registry;
	}

	private BeanDefinitionRegistry getExternalRegistry() {
		if(registry instanceof HierarchicalBeanFactory && ((HierarchicalBeanFactory) registry).getParentBeanFactory() != null)
			return (BeanDefinitionRegistry) ((HierarchicalBeanFactory)registry).getParentBeanFactory();
		return registry;
	}

	/**
	 * @param registry
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int loadBeanDefinitions(ConfigurationModel model) {
		int initialBeanDefCount = getInternalRegistry().getBeanDefinitionCount();

		for(ConfigurationClass configClass : model.getAllConfigurationClasses())
			loadBeanDefinitionsForConfigurationClass(configClass);

		for(AspectClass aspectClass : model.getAspectClasses())
			loadBeanDefinitionsForAspectClass(aspectClass);

		return getInternalRegistry().getBeanDefinitionCount() - initialBeanDefCount;
	}

	private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass) {
		loadBeanDefinitionsForDeclaringClass(configClass.getDeclaringClass());

		String configClassName = configClass.getName();

		doLoadBeanDefinitionForConfigurationClass(configClassName);

		for(ResourceBundles resourceBundles : configClass.getResourceBundles())
			loadBeanDefinitionsForResourceBundles(resourceBundles);

		for(BeanMethod beanMethod : configClass.getBeanMethods())
			loadBeanDefinitionsForBeanMethod(configClass, configClassName, beanMethod);

		for(AutoBeanMethod autoBeanMethod : configClass.getAutoBeanMethods())
			loadBeanDefinitionsForAutoBeanMethod(autoBeanMethod);
	}

	private void doLoadBeanDefinitionForConfigurationClass(String configClassName) {
		RootBeanDefinition configBeanDef = new RootBeanDefinition();
		configBeanDef.setBeanClassName(configClassName);
		// mark this bean def with metadata indicating that it is a configuration bean
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));

		// @Configuration classes' bean names are always their fully-qualified classname
		getExternalRegistry().registerBeanDefinition(configClassName, configBeanDef);
	}

	private void loadBeanDefinitionsForResourceBundles(ResourceBundles resourceBundles) {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setResourceLoader(new DefaultResourceLoader());
		ms.setBasenames(resourceBundles.value());
		MessageSourceValueSource valueSource = new MessageSourceValueSource(ms);
		((SingletonBeanRegistry)getInternalRegistry()).registerSingleton("valueSource", valueSource);
	}

	private void loadBeanDefinitionsForBeanMethod(ConfigurationClass configClass,
	                                              String configClassName,
	                                              BeanMethod beanMethod) {
		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setFactoryBeanName(configClassName);
		beanDef.setFactoryMethodName(beanMethod.getName());

		Bean metadata = beanMethod.getMetadata();
		Configuration defaults = configClass.getMetadata();

		// consider scoping
		beanDef.setScope(metadata.scope());

		// consider autowiring
		if(metadata.autowire() != AnnotationUtils.getDefaultValue(Bean.class, "autowire"))
			beanDef.setAutowireMode(metadata.autowire().value());
		else
			if(defaults.defaultAutowire() != AnnotationUtils.getDefaultValue(Configuration.class, "defaultAutowire"))
				beanDef.setAutowireMode(defaults.defaultAutowire().value());

		// TODO: plug in NamingStrategy here
		String beanName = beanMethod.getName();

		// consider aliases
		for(String alias : metadata.aliases())
			getExternalRegistry().registerAlias(beanName, alias);

		// is this bean marked as primary for disambiguation?
		if(metadata.primary() == Primary.TRUE)
			beanDef.setPrimary(true);

		// is this method annotated with @ScopedProxy?
		if(beanMethod.isScopedProxy()) {
			RootBeanDefinition targetDef = beanDef;

			// Create a scoped proxy definition for the original bean name,
			// "hiding" the target bean in an internal target definition.
			String targetBeanName = ScopedProxyMethodProcessor.resolveHiddenScopedProxyBeanName(beanName);
			RootBeanDefinition scopedProxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
			scopedProxyDefinition.getPropertyValues().addPropertyValue("targetBeanName", targetBeanName);

			// transfer relevant attributes from original bean to scoped-proxy bean
			scopedProxyDefinition.setScope(beanMethod.getMetadata().scope());

			if (beanMethod.getScopedProxyMetadata().proxyTargetClass())
				targetDef.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
				// ScopedFactoryBean's "proxyTargetClass" default is TRUE, so we
				// don't need to set it explicitly here.
			else
				scopedProxyDefinition.getPropertyValues().addPropertyValue("proxyTargetClass", Boolean.FALSE);

			// The target bean should be ignored in favor of the scoped proxy.
			targetDef.setAutowireCandidate(false);

			// Register the target bean as separate bean in the factory
			getExternalRegistry().registerBeanDefinition(targetBeanName, targetDef);

			// replace the original bean definition with the target one
			beanDef = scopedProxyDefinition;
		}

		if(Modifier.isPublic(beanMethod.getModifiers()))
			getExternalRegistry().registerBeanDefinition(beanName, beanDef);
		else
			getInternalRegistry().registerBeanDefinition(beanName, beanDef);
	}

	private void loadBeanDefinitionsForAutoBeanMethod(AutoBeanMethod method) {
		Type returnType = method.getReturnType();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(returnType.getName());
		beanDef.setAutowireMode(method.getMetadata().autowire().value());

		getExternalRegistry().registerBeanDefinition(method.getName(), beanDef);
	}

	private void loadBeanDefinitionsForAspectClass(AspectClass aspectClass) {
		String className = aspectClass.getName();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(className);

		// @Aspect classes' bean names are always their fully-qualified classname
		// don't overwrite any existing bean definition (in the case of an @Aspect @Configuration)
		if(!getExternalRegistry().containsBeanDefinition(className))
			getExternalRegistry().registerBeanDefinition(className, beanDef);
	}

	private void loadBeanDefinitionsForDeclaringClass(ConfigurationClass declaringClass) {
		if(declaringClass == null)
			return;

		logger.info(format("Found declaring class [%s] on configClass [%s]", declaringClass, declaringClass));

		BeanFactory parentBF;
		String factoryName = BeanFactoryFactory.class.getName();
		if(((ConfigurableListableBeanFactory)getInternalRegistry()).containsBean(factoryName))
			parentBF = (BeanFactory) ((ConfigurableListableBeanFactory)getInternalRegistry()).getBean(factoryName, new Object[] { declaringClass.getName() });
		else
			parentBF = new DefaultListableBeanFactory();

		((ConfigurableListableBeanFactory)getExternalRegistry()).setParentBeanFactory(parentBF);

		// TODO: test for the case where more than one configuration class has a declaring class - this should be illegal
		// because it would result in setParentBeanFactory being called more than once.
		// note that this violation should be detected at model validation time, not at rendering time - that's too late.
	}

}
