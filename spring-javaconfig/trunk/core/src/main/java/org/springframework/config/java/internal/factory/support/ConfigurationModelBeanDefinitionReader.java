package org.springframework.config.java.internal.factory.support;


import static java.lang.String.format;
import static org.springframework.config.java.internal.factory.BeanVisibility.PUBLIC;
import static org.springframework.config.java.internal.factory.BeanVisibility.visibilityOf;
import static org.springframework.util.StringUtils.hasText;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.config.java.annotation.Primary;
import org.springframework.config.java.annotation.ResourceBundles;
import org.springframework.config.java.internal.factory.BeanFactoryProvider;
import org.springframework.config.java.internal.factory.BeanVisibility;
import org.springframework.config.java.internal.factory.JavaConfigBeanFactory;
import org.springframework.config.java.internal.model.AspectClass;
import org.springframework.config.java.internal.model.AutoBeanMethod;
import org.springframework.config.java.internal.model.BeanMethod;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.config.java.internal.model.ConfigurationModel;
import org.springframework.config.java.internal.type.Type;
import org.springframework.config.java.internal.util.Constants;
import org.springframework.config.java.valuesource.MessageSourceValueSource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;

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

	private final JavaConfigBeanFactory beanFactory;

	public ConfigurationModelBeanDefinitionReader(JavaConfigBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @param model
	 * @return number of bean definitions generated
	 */
	public int loadBeanDefinitions(ConfigurationModel model) {
		int initialBeanDefCount = beanFactory.getBeanDefinitionCount();

		for(ConfigurationClass configClass : model.getAllConfigurationClasses())
			loadBeanDefinitionsForConfigurationClass(configClass);

		for(AspectClass aspectClass : model.getAspectClasses())
			loadBeanDefinitionsForAspectClass(aspectClass);

		return beanFactory.getBeanDefinitionCount() - initialBeanDefCount;
	}

	private void loadBeanDefinitionsForConfigurationClass(ConfigurationClass configClass) {
		loadBeanDefinitionsForDeclaringClass(configClass.getDeclaringClass());

		doLoadBeanDefinitionForConfigurationClass(configClass.getName());

		for(ResourceBundles resourceBundles : configClass.getResourceBundles())
			loadBeanDefinitionsForResourceBundles(resourceBundles);

		for(BeanMethod beanMethod : configClass.getBeanMethods())
			loadBeanDefinitionsForBeanMethod(configClass, beanMethod);

		for(AutoBeanMethod autoBeanMethod : configClass.getAutoBeanMethods())
			loadBeanDefinitionsForAutoBeanMethod(autoBeanMethod);
	}

	private void doLoadBeanDefinitionForConfigurationClass(String configClassName) {
		/* TODO: come back and clean this commenting up - left in for future use when re-enabling XML bootstrapping
		ArrayList<BeanDefinition> existingBeanDefs = new ArrayList<BeanDefinition>();
		for(String beanName : getExternalRegistry().getBeanDefinitionNames()) {
			BeanDefinition beanDef = getExternalRegistry().getBeanDefinition(beanName);
			if(beanDef.getBeanClassName() != null && beanDef.getBeanClassName().equals(configClassName)) {
				existingBeanDefs.add(beanDef);
			}
		}

		*/
		RootBeanDefinition configBeanDef;
		/*

		switch (existingBeanDefs.size()) {
			case 0:
		*/
				configBeanDef = new RootBeanDefinition();
				configBeanDef.setBeanClassName(configClassName);
		/*
				break;
			case 1:
				configBeanDef = (GenericBeanDefinition) existingBeanDefs.get(0);
				break;
			default:
				throw new IllegalStateException("multiple bean definitions already exist for " + configClassName);
		}
		*/

		// mark this bean def with metadata indicating that it is a configuration bean
		configBeanDef.addMetadataAttribute(new BeanMetadataAttribute(ConfigurationClass.BEAN_ATTR_NAME, true));

		// @Configuration classes' bean names are always their fully-qualified classname
		if(logger.isInfoEnabled())
			logger.info(format("Registering %s bean definition for @Configuration class %s", PUBLIC, configClassName));
		beanFactory.registerBeanDefinition(configClassName, configBeanDef, PUBLIC);
	}

	private void loadBeanDefinitionsForResourceBundles(ResourceBundles resourceBundles) {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setResourceLoader(new DefaultResourceLoader());
		ms.setBasenames(resourceBundles.value());
		MessageSourceValueSource valueSource = new MessageSourceValueSource(ms);
		if(logger.isInfoEnabled())
			logger.info(format("Registering singleton ValueSource %s for @ResourceBundles %s", valueSource, resourceBundles));
		beanFactory.registerSingleton("valueSource", valueSource);
	}

	private void loadBeanDefinitionsForBeanMethod(ConfigurationClass configClass, BeanMethod beanMethod) {
		String configClassName = configClass.getName();

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
		else if(defaults.defaultAutowire() != AnnotationUtils.getDefaultValue(Configuration.class, "defaultAutowire"))
			beanDef.setAutowireMode(defaults.defaultAutowire().value());

		String beanName = beanFactory.getBeanNamingStrategy().getBeanName(beanMethod);

		// has this already been overriden (i.e.: via XML)?
		if(containsBeanDefinitionIncludingAncestry(beanFactory, beanName)) {
			BeanDefinition existingBeanDef = getBeanDefinitionIncludingAncestry(beanFactory, beanName);

			// is the existing bean definition one that was created by JavaConfig?
			if(existingBeanDef.getAttribute(Constants.JAVA_CONFIG_PKG) == null) {
				// no -> then it's an external override, probably XML

				// ensure that overriding is ok
				if(metadata.allowOverriding() == false)
					throw new IllegalStateException(format("Illegal attempt to override @Bean method %s.%s(). " +
							"allowOverride is set to 'false', consider setting to 'true'.",
							configClass.getName(), beanMethod.getName()));

				// overriding is legal, return immediately
        		logger.info(format("Skipping loading bean definition for %s: a definition for bean '%s' already exists. " +
        					"This is likely due to an override in XML.",
        				beanMethod, beanName));
				return;
			}
		}

		// consider aliases
		for(String alias : metadata.aliases())
			beanFactory.registerAlias(beanName, alias, PUBLIC);

		// is this bean marked as primary for disambiguation?
		if(metadata.primary() == Primary.TRUE)
			beanDef.setPrimary(true);

		// is this bean lazily instantiated?
		if(metadata.lazy() == Lazy.TRUE ||
				(metadata.lazy() == Lazy.UNSPECIFIED && defaults.defaultLazy() == Lazy.TRUE))
			beanDef.setLazyInit(true);

		// does this bean have a custom init-method specified?
		String initMethodName = metadata.initMethodName();
		if(hasText(initMethodName)) beanDef.setInitMethodName(initMethodName);

		// does this bean have a custom destroy-method specified?
		String destroyMethodName = metadata.destroyMethodName();
		if(hasText(destroyMethodName)) beanDef.setDestroyMethodName(destroyMethodName);

		// is this method annotated with @ScopedProxy?
		if(beanMethod.isScopedProxy()) {
			RootBeanDefinition targetDef = beanDef;

			// Create a scoped proxy definition for the original bean name,
			// "hiding" the target bean in an internal target definition.
			String targetBeanName = ConfigurationModelBeanDefinitionReader.resolveHiddenScopedProxyBeanName(beanName);
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
			beanFactory.registerBeanDefinition(targetBeanName, targetDef, PUBLIC);

			// replace the original bean definition with the target one
			beanDef = scopedProxyDefinition;
		}

		BeanVisibility visibility = visibilityOf(beanMethod.getModifiers());
		logger.info(format("Registering %s bean definition for @Bean method %s.%s()", visibility, configClassName, beanName));
		beanFactory.registerBeanDefinition(beanName, beanDef, visibility);
	}

	private static boolean containsBeanDefinitionIncludingAncestry(JavaConfigBeanFactory beanFactory, String beanName) {
		try {
			getBeanDefinitionIncludingAncestry(beanFactory, beanName);
			return true;
		} catch (NoSuchBeanDefinitionException ex) {
			return false;
		}
	}

	private static BeanDefinition getBeanDefinitionIncludingAncestry(JavaConfigBeanFactory beanFactory, String beanName) {
		ConfigurableListableBeanFactory bf = beanFactory;
		do {
			if(bf.containsBeanDefinition(beanName))
				return bf.getBeanDefinition(beanName);
			BeanFactory parent = bf.getParentBeanFactory();
			if(parent == null) {
				bf = null;
			}
			else if (parent instanceof ConfigurableListableBeanFactory) {
				bf = (ConfigurableListableBeanFactory) parent;
			}
			else if (parent instanceof AbstractApplicationContext) {
				bf = ((AbstractApplicationContext) parent).getBeanFactory();
			}
			else {
				throw new IllegalStateException("unknown parent type: " + parent.getClass().getName());
			}
		} while (bf != null);

		throw new NoSuchBeanDefinitionException(format("No bean definition matching name '%s' could be found in %s or its ancestry", beanName, beanFactory));
	}


	private void loadBeanDefinitionsForAutoBeanMethod(AutoBeanMethod method) {
		Type returnType = method.getReturnType();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(returnType.getName());
		beanDef.setAutowireMode(method.getMetadata().autowire().value());

		beanFactory.registerBeanDefinition(method.getName(), beanDef, PUBLIC);
	}

	private void loadBeanDefinitionsForAspectClass(AspectClass aspectClass) {
		String className = aspectClass.getName();

		RootBeanDefinition beanDef = new RootBeanDefinition();
		beanDef.setBeanClassName(className);

		// @Aspect classes' bean names are always their fully-qualified classname
		// don't overwrite any existing bean definition (in the case of an @Aspect @Configuration)
		if(!beanFactory.containsBeanDefinition(className, PUBLIC))
			beanFactory.registerBeanDefinition(className, beanDef, PUBLIC);
	}

	private void loadBeanDefinitionsForDeclaringClass(ConfigurationClass declaringClass) {
		if(declaringClass == null)
			return;

		logger.info(format("Found declaring class [%s] on configClass [%s]", declaringClass, declaringClass));

		BeanFactory parentBF;
		String factoryName = BeanFactoryProvider.BEAN_NAME;

		if(beanFactory.containsBean(factoryName))
			parentBF = (BeanFactory) beanFactory.getBean(factoryName, new Object[] { declaringClass.getName() });
		else
			parentBF = new DefaultListableBeanFactory();

		beanFactory.getParentBeanFactory().setParentBeanFactory(parentBF);

		// TODO: test for the case where more than one configuration class has a declaring class - this should be illegal
		// because it would result in setParentBeanFactory being called more than once.
		// note that this violation should be detected at model validation time, not at rendering time - that's too late.
	}

	/**
	 * Return the <i>hidden</i> name based on a scoped proxy bean name.
	 *
	 * @param originalBeanName the scope proxy bean name as declared in the
	 * Configuration-annotated class
	 *
	 * @return the internally-used <i>hidden</i> bean name
	 */
	public static String resolveHiddenScopedProxyBeanName(String originalBeanName) {
		Assert.hasText(originalBeanName);
		return TARGET_NAME_PREFIX.concat(originalBeanName);
	}

	private static final String TARGET_NAME_PREFIX = "scopedTarget.";


}
