package org.springframework.webflow.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.expression.Expression;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Makes a {@link FlowRedirect} response selection when requested;
 * calculating the flowId and flow input by evaluating an expression against
 * the request context.
 * 
 * @author Keith Donald
 */
public class FlowRedirectSelector implements ViewSelector {

	/**
	 * The parsed flow expression, evaluatable to the string format:
	 * flowId?param1Name=parmValue&param2Name=paramValue
	 */
	private Expression flowExpression;

	/**
	 * Creates a new flow redirect selector
	 * @param flowExpression the parsed flow redirect expression,
	 * evaluatable to the string format:
	 * flowId?param1Name=parmValue&param2Name=paramValue
	 */
	public FlowRedirectSelector(Expression flowExpression) {
		this.flowExpression = flowExpression;
	}

	public ViewSelection makeSelection(RequestContext context) {
		String flowRedirect = (String)flowExpression.evaluateAgainst(context, Collections.EMPTY_MAP);
		// the encoded flowRedirect should look something like
		// "flowId?param0=value0&param1=value1"
		// now parse that and build a corresponding view selection
		int index = flowRedirect.indexOf('?');
		String flowId;
		Map input = null;
		if (index != -1) {
			flowId = flowRedirect.substring(0, index);
			String[] parameters = StringUtils.delimitedListToStringArray(flowRedirect.substring(index + 1), "&");
			input = new HashMap(parameters.length, 1);
			for (int i = 0; i < parameters.length; i++) {
				String nameAndValue = parameters[i];
				index = nameAndValue.indexOf('=');
				if (index != -1) {
					input.put(nameAndValue.substring(0, index), nameAndValue.substring(index + 1));
				}
				else {
					input.put(nameAndValue, "");
				}
			}
		}
		else {
			flowId = flowRedirect;
		}
		if (!StringUtils.hasText(flowId)) {
			// equivalent to restart
			flowId = context.getFlowExecutionContext().getFlow().getId();
		}
		return new FlowRedirect(flowId, input);
	}
}