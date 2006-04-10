package org.springframework.webflow.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.FlowVariable;
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
	 * The bean name in the factory whose value will be used as the flow variable. 
	 */
	private String beanName;
	
	/**
	 * The bean factory where initial variable values will be obtained.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a new flow variable.
	 * @param variableName the variable name
	 * @param scope the variable scope
	 * @param beanName the bean name
	 * @param beanFactory the bean factory where initial variable values will be obtained
	 */
	public BeanFactoryFlowVariable(String variableName, ScopeType scope, String beanName, BeanFactory beanFactory) {
		super(variableName, scope);
		if (StringUtils.hasText(beanName)) {
			this.beanName = beanName;
		} else {
			this.beanName = variableName;
		}
		Assert.notNull(beanFactory, "The variable bean factory is required");
		Assert.isTrue(!beanFactory.isSingleton(variableName), "The variable bean must be a prototype (singleton=false)");
		this.beanFactory = beanFactory;
	}

	protected Object createVariableValue(RequestContext context) {
		return beanFactory.getBean(beanName);
	}
}