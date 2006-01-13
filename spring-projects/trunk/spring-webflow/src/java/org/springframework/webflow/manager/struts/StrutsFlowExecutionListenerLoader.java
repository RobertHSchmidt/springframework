package org.springframework.webflow.manager.struts;

import org.springframework.webflow.manager.ConditionalFlowExecutionListenerLoader;

/**
 * Simple extension of {@link ConditionalFlowExecutionListenerLoader} that adds
 * a {@link ActionFormAdapter} listener that applies to all flows.
 * <p>
 * This adapter is typically required when using Struts and Spring Web Flow
 * together.
 * @author Keith Donald
 */
public class StrutsFlowExecutionListenerLoader extends ConditionalFlowExecutionListenerLoader {
	public StrutsFlowExecutionListenerLoader() {
		addListener(new ActionFormAdapter());
	}
}
