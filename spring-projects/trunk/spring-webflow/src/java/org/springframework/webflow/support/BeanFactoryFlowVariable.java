package org.springframework.webflow.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * A concrete flow variable subclass that obtains variable values from a Spring
 * {@link BeanFactory}.
 * 
 * @author Keith Donald
 */
public class BeanFactoryFlowVariable extends FlowVariable {

	/**
	 * The bean factory where initial variable values will be obtained.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 * @param beanFactory the bean factory where initial variable values will be obtained
	 */
	public BeanFactoryFlowVariable(String name, BeanFactory beanFactory) {
		this(name, ScopeType.FLOW, beanFactory);
	}

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 * @param scope the variable scope
	 * @param beanFactory the bean factory where initial variable values will be obtained
	 */
	public BeanFactoryFlowVariable(String name, ScopeType scope, BeanFactory beanFactory) {
		super(name, scope);
		Assert.notNull(beanFactory, "The variable bean factory is required");
		Assert.isTrue(!beanFactory.isSingleton(name), "The variable bean must be a prototype (singleton=false)");
		this.beanFactory = beanFactory;
	}

	protected Object createVariableValue(RequestContext context) {
		return beanFactory.getBean(getName());
	}
}