package org.springframework.webflow.tapestry.binding;

import org.apache.hivemind.Location;
import org.apache.tapestry.IPage;
import org.apache.tapestry.binding.AbstractBinding;
import org.apache.tapestry.coerce.ValueConverter;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.Scope;

public class FlowScopeBinding extends AbstractBinding {

	private IPage page;

	private String attributeName;

	public FlowScopeBinding(IPage page, String attributeName,
			String description, ValueConverter valueConverter, Location location) {
		super(description, valueConverter, location);
		this.page = page;
		this.attributeName = attributeName;
	}

	public IPage getPage() {
		return page;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Object getObject() {
		return getFlowScope().getAttribute(getAttributeName());
	}

	public void setObject(Object value) {
		getFlowScope().setAttribute(getAttributeName(), value);
	}
	
	protected Scope getFlowScope() {
		FlowExecutionContext flowExecution = (FlowExecutionContext) getPage().getProperty(
				"flowExecutionContext");
		return flowExecution.getActiveSession().getScope();
	}
}