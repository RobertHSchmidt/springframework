package org.springframework.webflow.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.LocalParameterMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class FlowDefinitionRedirectAction extends AbstractAction {
	private Expression flowId;
	private Expression[] requestElements;
	private Map requestParameters;

	public FlowDefinitionRedirectAction(Expression flowId, Expression[] requestElements, Map requestParameters) {
		Assert.notNull(flowId, "The flow id to redirect to is required");
		this.flowId = flowId;
		this.requestElements = requestElements;
		this.requestParameters = requestParameters;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		String flowId = (String) this.flowId.evaluate(context, null);
		String[] requestElements = evaluateRequestElements(context);
		ParameterMap requestParameters = evaluateRequestParameters(context);
		context.getExternalContext().sendFlowDefinitionRedirect(flowId, requestElements, requestParameters);
		return success();
	}

	private String[] evaluateRequestElements(RequestContext context) {
		if (this.requestElements == null) {
			return null;
		}
		String[] requestElements = new String[this.requestElements.length];
		for (int i = 0; i < this.requestElements.length; i++) {
			Expression element = this.requestElements[i];
			requestElements[i] = (String) element.evaluate(context, null);
		}
		return requestElements;
	}

	private ParameterMap evaluateRequestParameters(RequestContext context) {
		if (this.requestParameters == null) {
			return null;
		} else {
			Map requestParameters = new HashMap();
			for (Iterator it = this.requestParameters.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				Expression name = (Expression) entry.getKey();
				Expression value = (Expression) entry.getValue();
				String paramName = (String) name.evaluate(context, null);
				String paramValue = (String) value.evaluate(context, null);
				requestParameters.put(paramName, paramValue);
			}
			return new LocalParameterMap(requestParameters);
		}
	}

	public static FlowDefinitionRedirectAction create(String encodedFlowRedirect) {
		// TODO
		return null;
	}
}
