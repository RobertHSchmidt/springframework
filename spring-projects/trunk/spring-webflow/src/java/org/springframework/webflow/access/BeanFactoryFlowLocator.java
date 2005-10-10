package org.springframework.webflow.access;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;

/**
 * A flow locator that retrieves its flow definitions from a standard Spring
 * BeanFactory. This implementation exists standalone as the FlowLocator is
 * needed at runtime for spawning FlowExecutions, while the FlowArtifactLocator
 * subinterface is only needed at configuration time.
 * @author Keith Donald
 */
public class BeanFactoryFlowLocator implements FlowLocator, BeanFactoryAware {

	/**
	 * The wrapped Spring bean factory to delegate to.
	 */
	private BeanFactory beanFactory;

	/**
	 * Default constructor for usage when instantiated by Spring.
	 */
	public BeanFactoryFlowLocator() {

	}

	/**
	 * Creates a flow locator that retrieves artifacts from the provided bean
	 * factory
	 * @param beanFactory The spring bean factory, may not be null.
	 */
	public BeanFactoryFlowLocator(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "The beanFactory is required");
		this.beanFactory = beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.beanFactory != null) {
			this.beanFactory = beanFactory;
		}
	}

	public Flow getFlow(String id) throws FlowArtifactLookupException {
		try {
			return (Flow)beanFactory.getBean(id, Flow.class);
		}
		catch (NoSuchBeanDefinitionException e) {
			throw new NoSuchFlowDefinitionException(id, e);
		}
		catch (BeansException e) {
			throw new FlowArtifactLookupException(Flow.class, id, e);
		}
	}
}