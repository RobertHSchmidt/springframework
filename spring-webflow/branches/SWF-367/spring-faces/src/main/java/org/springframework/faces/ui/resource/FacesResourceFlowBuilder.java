package org.springframework.faces.ui.resource;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.engine.builder.support.AbstractFlowBuilder;

public class FacesResourceFlowBuilder extends AbstractFlowBuilder {

	private static final String FLOW_ID = "faces-resources";

	public void buildStates() throws FlowBuilderException {

		Flow resourcesFlow = getFlow();

		EndState endState = new EndState(resourcesFlow, "renderResource");
		endState.setFinalResponseAction(new ResolveAndRenderResourceAction());
	}

	public void init(String flowId, AttributeMap attributes) throws FlowBuilderException {
		Flow resourcesFlow = new Flow(FLOW_ID);
		setFlow(resourcesFlow);
	}

}
