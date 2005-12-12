package org.springframework.webflow.support;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * A value object that defines a flow variable. Encapsulates information about
 * the variable (name and type), and the behaivior to create a new variable
 * instance in flow execution scope.
 * @author Keith Donald
 */
public class FlowVariable {

	/**
	 * The variable name.
	 */
	private String name;

	/**
	 * The variable type.
	 */
	private Class type;

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 * @param type the variable type
	 */
	public FlowVariable(String name, Class type) {
		Assert.notNull(name, "The variable name is required");
		Assert.notNull(type, "The variable type is required");
		this.name = name;
		this.type = type;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowVariable)) {
			return false;
		}
		FlowVariable other = (FlowVariable)o;
		return this.name.equals(other.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Creates a new instance of this flow variable in flow scope.
	 * @param context the flow execution request context
	 */
	public void create(RequestContext context) {
		context.getFlowScope().setAttribute(name, newInstance(type, context));
	}

	protected Object newInstance(Class type, RequestContext context) {
		return BeanUtils.instantiateClass(type);
	}
}