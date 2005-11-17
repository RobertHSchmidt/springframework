package org.springframework.webflow.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

/**
 * A flow artifact locator that pulls its artifacts from a standard Spring
 * BeanFactory.
 * @author Keith Donald
 */
public class DefaultFlowArtifactFactory extends AbstractFlowArtifactFactory {

	/**
	 * The Spring bean factory.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact locator that retrieves artifacts from the
	 * provided bean factory
	 * @param beanFactory The spring bean factory, may not be null.
	 * @param subflowLocator The locator for loading subflows
	 */
	public DefaultFlowArtifactFactory(FlowLocator subflowLocator, BeanFactory beanFactory) {
		super(subflowLocator);
		Assert.notNull(beanFactory, "The beanFactory to retrieve flow artifacts is required");
		this.beanFactory = beanFactory;
	}

	protected Object doGetArtifact(String id, Class artifactType) {
		return beanFactory.getBean(id);
	}
}