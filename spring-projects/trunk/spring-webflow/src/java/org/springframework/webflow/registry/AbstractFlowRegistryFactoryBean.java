package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.webflow.builder.FlowArtifactFactory;

/**
 * A base class for factory beans that create populated Flow Registries.
 * Subclasses should override the {@link #doPopulate(FlowRegistry)}
 * to perform the registry population logic, typically delegating to a
 * {@link FlowRegistrar} strategy.
 * 
 * @author Keith Donald
 */
public abstract class AbstractFlowRegistryFactoryBean implements FactoryBean, BeanFactoryAware {

	/**
	 * The flow registry to register Flow definitions in.
	 */
	private FlowRegistryImpl flowRegistry = new FlowRegistryImpl();

	/**
	 * Strategy for locating dependent artifacts when a Flow is being built.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Creates a flow registry factory bean.
	 */
	public AbstractFlowRegistryFactoryBean() {

	}

	/**
	 * Sets the parent registry of the registry constructed by this factory bean.
	 * @param parent the parent flow definition registry
	 */
	public void setParent(FlowRegistry parent) {
		this.flowRegistry.setParent(parent);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.flowArtifactFactory = new FlowRegistryFlowArtifactFactory(getFlowRegistry(), beanFactory);
	}

	/**
	 * Returns the flow registry constructed by the factory bean.
	 */
	protected FlowRegistry getFlowRegistry() {
		return flowRegistry;
	}

	/**
	 * Returns the strategy for locating dependent artifacts when a Flow is
	 * being built.
	 */
	protected FlowArtifactFactory getFlowArtifactFactory() {
		return this.flowArtifactFactory;
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
}