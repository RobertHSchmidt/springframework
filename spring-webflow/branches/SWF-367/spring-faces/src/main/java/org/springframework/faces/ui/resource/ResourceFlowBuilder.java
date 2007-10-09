package org.springframework.faces.ui.resource;

import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.FlowBuilderException;
import org.springframework.webflow.engine.builder.support.AbstractFlowBuilder;

/**
 * Builder for generating the "resources" flow which is responsible for serving static resources from the classpath.
 * @author Jeremy Grelle
 */
public class ResourceFlowBuilder extends AbstractFlowBuilder {

	protected Flow createFlow() {
		return new Flow("resources");
	}

	public void buildStates() throws FlowBuilderException {
		Flow resourcesFlow = getFlow();
		EndState endState = new EndState(resourcesFlow, "renderResource");
		endState.setFinalResponseAction(new ResolveAndRenderResourceAction());
	}
}
