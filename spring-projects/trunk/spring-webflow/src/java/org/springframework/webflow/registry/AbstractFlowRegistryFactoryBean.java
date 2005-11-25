package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.config.FlowArtifactFactory;

/**
 * A base class for factory beans that create populated Flow Registries.
 * Subclasses should override the {@link #doPopulate(FlowRegistry)}
 * to perform the registry population logic, typically delegating to a
 * {@link FlowRegistrar} strategy.
 * 
 * This class is also <code>BeanFactoryAware</code> and when used with Spring
 * will automatically create a configured {@link FlowRegistryFlowArtifactFactory}
 * for loading Flow artifacts like Actions from the Spring bean factory during
 * the Flow registration process.
 * 
 * @see FlowRegistrar
 * @see FlowArtifactFactory
 * @see FlowRegistryFlowArtifactFactory
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFactoryBean implements FactoryBean, BeanFactoryAware, InitializingBean {

	/**
	 * The flow registry to register Flow definitions in.
	 */
	private FlowRegistryImpl flowRegistry = new FlowRegistryImpl();

	/**
	 * The flow artifact factory.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Creates a flow registry factory bean.
	 */
	public AbstractFlowRegistryFactoryBean() {

	}

	/**
	 * Creates a flow registry factory bean, for programmatic usage only.
	 * @param beanFactory the bean factory to use for locating flow artifacts.
	 */
	public AbstractFlowRegistryFactoryBean(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	/**
	 * Sets the parent registry of the registry constructed by this factory bean.
	 * @param parent the parent flow definition registry
	 */
	public void setParent(FlowRegistry parent) {
		this.flowRegistry.setParent(parent);
	}

	/**
	 * Returns the flow registry constructed by the factory bean.
	 */
	protected FlowRegistry getFlowRegistry() {
		return flowRegistry;
	}

	/**
	 * Returns the flow artifact factory strategy to use to access flow artifacts during the registry 
	 * population process.
	 */
	protected FlowArtifactFactory getFlowArtifactFactory() {
		return flowArtifactFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.flowArtifactFactory = new FlowRegistryFlowArtifactFactory(getFlowRegistry(), beanFactory);
	}

	public Object getObject() throws Exception {
		return populateFlowRegistry();
	}

	/**
	 * Populates and returns the configured flow definition registry.
	 */
	public FlowRegistry populateFlowRegistry() {
		doPopulate(getFlowRegistry());
		return getFlowRegistry();
	}

	/**
	 * Template method subclasses must override to perform registry
	 * population.
	 * @param registry the flow definition registry
	 */
	protected abstract void doPopulate(FlowRegistry registry);

	public Class getObjectType() {
		return FlowRegistry.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		init();
	}

	/**
	 * Custom initializing hook called automatically when Spring instantiates
	 * this factory bean. Subclasses may override.
	 */
	protected void init() {

	}
}