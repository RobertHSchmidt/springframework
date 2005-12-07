package org.springframework.webflow;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

public class FlowVariable {
	private String name;

	private Class type;

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
	
	public void create(FlowExecutionControlContext context) {
		context.getFlowScope().setAttribute(name, newInstance(type));
	}
	
	protected Object newInstance(Class type) {
		return BeanUtils.instantiateClass(type);
	}
}