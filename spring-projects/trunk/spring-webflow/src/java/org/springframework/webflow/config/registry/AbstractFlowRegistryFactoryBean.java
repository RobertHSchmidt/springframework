package org.springframework.webflow.config.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.config.BeanFactoryFlowArtifactLocator;
import org.springframework.webflow.config.FlowArtifactLocator;

/**
 * A factory bean that produces a populated Flow Registry using a XML flow
 * definition registrar.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	/**
	 * The flow registry to register Flow definitions in.
	 */
	private FlowRegistry registry = new FlowRegistryImpl();

	/**
	 * The flow artifact locator.
	 */
	private FlowArtifactLocator artifactLocator;

	/**
	 * Creates a xml flow registry factory bean.
	 */
	public AbstractFlowRegistryFactoryBean() {

	}

	/**
	 * Creates a xml flow registry factory bean.
	 * @param beanFactory the bean factory to use for locating flow artifacts.
	 */
	public AbstractFlowRegistryFactoryBean(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	protected FlowRegistry getFlowRegistry() {
		return registry;
	}

	protected FlowArtifactLocator getFlowArtifactLocator() {
		return artifactLocator;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (artifactLocator == null) {
			this.artifactLocator = new BeanFactoryFlowArtifactLocator(beanFactory, getFlowRegistry());
		}
	}

	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public FlowRegistry populateFlowRegistry() {
		registerFlowDefinitions(getFlowRegistry());
		return getFlowRegistry();
	}

	/**
	 * Template method subclasses must override to perform registry
	 * registration.
	 * @param registry the flow definition registry
	 */
	protected abstract void registerFlowDefinitions(FlowRegistry registry);

	public Class getObjectType() {
		return FlowRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		init();
	}

	protected void init() {

	}
}