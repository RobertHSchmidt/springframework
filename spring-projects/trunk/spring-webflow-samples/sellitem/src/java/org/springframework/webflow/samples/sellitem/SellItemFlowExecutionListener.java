package org.springframework.webflow.samples.sellitem;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.execution.EnterStateVetoException;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.servlet.ServletEvent;

public class SellItemFlowExecutionListener extends FlowExecutionListenerAdapter {
	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
		String role = (String)nextState.getProperty("role");
		if (StringUtils.hasText(role)) {
			HttpServletRequest request = ((ServletEvent)context.getSourceEvent()).getRequest();
			if (!request.isUserInRole(role)) {
				throw new EnterStateVetoException(nextState, "State requires role '" + role
						+ "', but the authenticated user doesn't have it!");
			}
		}
	}
}
