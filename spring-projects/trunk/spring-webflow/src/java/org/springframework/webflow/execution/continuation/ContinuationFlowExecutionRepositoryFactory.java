package org.springframework.webflow.execution.continuation;

import org.springframework.webflow.execution.FlowExecutionRepository;
import org.springframework.webflow.execution.FlowExecutionRepositoryFactory;

public class ContinuationFlowExecutionRepositoryFactory implements FlowExecutionRepositoryFactory {
	public FlowExecutionRepository createRepository() {
		return new ContinuationFlowExecutionRepository();
	}
}
