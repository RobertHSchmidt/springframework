package org.springframework.webflow.samples.flowlauncher;

import org.springframework.webflow.execution.ConditionalFlowExecutionListenerLoader;

public class FlowExecutionListenerLoader extends ConditionalFlowExecutionListenerLoader {
	public FlowExecutionListenerLoader() {
		addListener(new SampleFlowExecutionListener());
	}
}
