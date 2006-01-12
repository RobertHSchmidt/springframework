package org.springframework.webflow.manager.jsf;

import javax.faces.context.FacesContext;

public class FlowExecutionHolderUtils {
	public static FlowExecutionHolder getFlowExecutionHolder(FacesContext context) {
		return (FlowExecutionHolder)context.getExternalContext().getRequestMap().get(getFlowExecutionHolderKey());
	}

	public static void setFlowExecutionHolder(FlowExecutionHolder holder, FacesContext context) {
		context.getExternalContext().getRequestMap().put(getFlowExecutionHolderKey(), holder);
	}

	private static String getFlowExecutionHolderKey() {
		return FlowExecutionHolder.class.getName();
	}
}
