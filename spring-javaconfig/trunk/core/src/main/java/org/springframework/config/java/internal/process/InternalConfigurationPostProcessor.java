package org.springframework.config.java.internal.process;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.config.java.internal.enhancement.CglibConfigurationEnhancer;
import org.springframework.config.java.internal.enhancement.ConfigurationEnhancer;
import org.springframework.config.java.internal.factory.BeanFactoryProvider;
import org.springframework.config.java.internal.factory.DefaultJavaConfigBeanFactory;
import org.springframework.config.java.internal.factory.JavaConfigBeanFactory;
import org.springframework.config.java.internal.factory.support.ReflectiveJavaConfigBeanDefinitionReader;
import org.springframework.config.java.internal.model.AspectClass;
import org.springframework.config.java.internal.model.ConfigurationClass;
import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

public class InternalConfigurationPostProcessor implements BeanFactoryPostProcessor {

	private static final Log logger = LogFactory.getLog(InternalConfigurationPostProcessor.class);

	private final AbstractApplicationContext externalContext;
	private final BeanNamingStrategy beanNamingStrategy;
	private final BeanFactoryProvider beanFactoryProvider;

	private final ArrayList<String> ignoredBeanPostProcessors = new ArrayList<String>();

	public InternalConfigurationPostProcessor(AbstractApplicationContext enclosingContext, BeanNamingStrategy beanNamingStrategy, BeanFactoryProvider beanFactoryProvider) {
		this.externalContext = enclosingContext;
		this.beanNamingStrategy = beanNamingStrategy;
		this.beanFactoryProvider = beanFactoryProvider;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) throws BeansException {
		JavaConfigBeanFactory internalBeanFactory = createInternalBeanFactory(externalBeanFactory);

		parseAnyConfigurationClasses(externalBeanFactory, internalBeanFactory);

		enhanceAnyConfigurationClasses(externalBeanFactory, internalBeanFactory);
	}

	public void addIgnoredBeanPostProcessor(String bppClassName) {
		ignoredBeanPostProcessors.add(bppClassName);
	}

	private JavaConfigBeanFactory createInternalBeanFactory(ConfigurableListableBeanFactory externalBeanFactory) {
		DefaultJavaConfigBeanFactory internalBeanFactory = new DefaultJavaConfigBeanFactory(externalBeanFactory, beanFactoryProvider);
		if(beanNamingStrategy != null)
			internalBeanFactory.setBeanNamingStrategy(beanNamingStrategy);

		// propagate any BeanPostProcessors from the external bean factory to the internal
		for (String bppName : externalBeanFactory.getBeanNamesForType(BeanPostProcessor.class))
			internalBeanFactory.registerSingleton(bppName, externalBeanFactory.getBean(bppName));

		wrapInternalBeanFactoryInApplicationContext(internalBeanFactory);

		return internalBeanFactory;
	}

	private void wrapInternalBeanFactoryInApplicationContext(DefaultJavaConfigBeanFactory internalBeanFactory) {
		final AbstractApplicationContext internalContext = new GenericApplicationContext(internalBeanFactory);
		internalContext.setDisplayName("JavaConfig internal application context");

		// add a listener that triggers this child context to refresh when the parent refreshes.
		// this will cause any BeanPostProcessors / BeanFactoryPostProcessors to be beansInvokedFor on
		// the beans in the child context.
		externalContext.addApplicationListener(new ChildContextRefreshingListener(externalContext, internalContext, ignoredBeanPostProcessors));
	}

	private void parseAnyConfigurationClasses(ConfigurableListableBeanFactory externalBeanFactory,
                                              JavaConfigBeanFactory internalBeanFactory) {
		ArrayList<ClassPathResource> aspectClassResources = new ArrayList<ClassPathResource>();
		ArrayList<ClassPathResource> configClassResources = new ArrayList<ClassPathResource>();

		for(String beanName : externalBeanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = externalBeanFactory.getBeanDefinition(beanName);
			if(beanDef.isAbstract())
				continue;
			if(beanDef.hasAttribute(ConfigurationClass.BEAN_ATTR_NAME)) {
				String path = ClassUtils.convertClassNameToResourcePath(beanDef.getBeanClassName());
				configClassResources.add(new ClassPathResource(path));
			} else if(beanDef.hasAttribute(AspectClass.BEAN_ATTR_NAME)) {
				String path = ClassUtils.convertClassNameToResourcePath(beanDef.getBeanClassName());
				aspectClassResources.add(new ClassPathResource(path));
			}
		}

		ReflectiveJavaConfigBeanDefinitionReader reader = new ReflectiveJavaConfigBeanDefinitionReader(internalBeanFactory, aspectClassResources);
		reader.loadBeanDefinitions(configClassResources.toArray(new ClassPathResource[configClassResources.size()]));
	}

    /**
     * Post-processes a BeanFactory in search of Configuration class BeanDefinitions;
     * any candidates are then enhanced by a {@link ConfigurationEnhancer}.  Candidate
     * status is determined by BeanDefinition attribute metadata.
     *
     * @see ConfigurationClass#BEAN_ATTR_NAME
     * @see ConfigurationEnhancer
     * @see BeanFactoryPostProcessor
     *
     * @author Chris Beams
     */
	private void enhanceAnyConfigurationClasses(ConfigurableListableBeanFactory externalBeanFactory,
                                                JavaConfigBeanFactory internalBeanFactory) {
		CglibConfigurationEnhancer enhancer = new CglibConfigurationEnhancer(internalBeanFactory);

		int configClassesEnhanced = 0;

		for(String beanName : externalBeanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDef = externalBeanFactory.getBeanDefinition(beanName);

			// is the beanDef marked as representing a configuration class?
			if(!beanDef.hasAttribute(ConfigurationClass.BEAN_ATTR_NAME))
				continue;

			String configClassName = beanDef.getBeanClassName();

			String enhancedClassName = enhancer.enhance(configClassName);

			if(logger.isDebugEnabled())
				logger.debug(String.format("Replacing bean definition class name [%s] with enhanced class name [%s]",
				                 configClassName, enhancedClassName));
			beanDef.setBeanClassName(enhancedClassName);

			configClassesEnhanced++;
		}

		if(configClassesEnhanced == 0)
			logger.warn("Found no @Configuration class BeanDefinitions within " + internalBeanFactory);
	}

}

/**
 * Listens for {@link ContextRefreshedEvent ContextRefreshedEvents} sourced from a parent application context
 * and propagates that event to the child.  Just prior to refreshing the child, searches for and adds any
 * {@link BeanFactoryPostProcessor} and {@link BeanPostProcessor} objects found in the parent context's
 * {@link BeanFactory}
 *
 * @author Chris Beams
 */
class ChildContextRefreshingListener implements ApplicationListener {

	private static final Log logger = LogFactory.getLog(ChildContextRefreshingListener.class);
	private final AbstractApplicationContext parent;
	private final AbstractApplicationContext child;
	private final List<String> ignoredBeanPostProcessors;

	public ChildContextRefreshingListener(AbstractApplicationContext parent, AbstractApplicationContext child, List<String> ignoredBeanPostProcessors) {
		this.parent = parent;
		this.child = child;
		this.ignoredBeanPostProcessors = ignoredBeanPostProcessors;
	}

	public void onApplicationEvent(ApplicationEvent event) {
		// only respond to ContextRefreshedEvents that are sourced from the parent context
		if(event.getSource() != parent) return;
		if(!(event instanceof ContextRefreshedEvent)) return;

		if(logger.isDebugEnabled())
			logger.debug(format("Caught ContextRefreshedEvent from parent application context [%s], now refreshing [%s]",
				parent.getDisplayName(), child.getDisplayName()));

		copyBeanFactoryPostProcessors();

		child.setParent(parent);
		child.refresh();
	}

	private void copyBeanFactoryPostProcessors() {
		// both of the following loops are necessary because BFPPs may have been added as singletons or as bean definitions
		// we must iterate over both possibilities in order to find all potential post processors.
		for(Object bfpp : parent.getBeanFactoryPostProcessors())
			doCopyBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);

		for (Object bfpp : parent.getBeansOfType(BeanFactoryPostProcessor.class).values())
			doCopyBeanFactoryPostProcessor((BeanFactoryPostProcessor) bfpp);
	}

	private void doCopyBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
		if(ignoredBeanPostProcessors.contains(postProcessor.getClass().getName()))
			return;

		logger.debug(String.format("copying BeanFactoryPostProcessor %s to child context %s", postProcessor, child));
		child.addBeanFactoryPostProcessor(postProcessor);
	}

}
