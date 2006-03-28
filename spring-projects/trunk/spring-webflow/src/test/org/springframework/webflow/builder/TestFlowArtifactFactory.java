package org.springframework.webflow.builder;

import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.Action;
import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.FlowSessionStatus;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.UnmodifiableAttributeMap;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.registry.NoSuchFlowDefinitionException;

/**
 * Flow service locator for the services needed by the testFlow (defined in
 * testFlow.xml)
 * 
 * @author Erwin Vervaet
 */
public class TestFlowArtifactFactory extends DefaultFlowArtifactFactory {

	public Flow getSubflow(String id) throws FlowArtifactException {
		if ("subFlow1".equals(id) || "subFlow2".equals(id)) {
			Flow flow = new Flow(id);
			new EndState(flow, "finish");
			return flow;
		}
		throw new NoSuchFlowDefinitionException(id, new String[] {"subFlow1", "subFlow2" });
	}

	public Action getAction(FlowArtifactParameters actionParameters) throws FlowArtifactException {
		String id = actionParameters.getId();
		if ("action1".equals(id) || "action2".equals(id)) {
			return new TestAction();
		}
		if ("multiAction".equals(id)) {
			return new TestMultiAction();
		}
		if ("pojoAction".equals(id)) {
			BeanInvokingActionParameters params = (BeanInvokingActionParameters)actionParameters;
			return toAction(new TestPojo(), params);
		}
		throw new FlowArtifactException(id, Action.class);
	}

	public boolean isMultiAction(String actionId) throws FlowArtifactException {
		return "multiAction".equals(actionId);
	}

	public boolean isStatefulAction(String actionId) throws FlowArtifactException {
		return false;
	}

	public FlowAttributeMapper getAttributeMapper(String id) throws FlowArtifactException {
		if ("attributeMapper1".equals(id)) {
			return new FlowAttributeMapper() {
				public AttributeMap createSubflowInput(RequestContext context) {
					return new AttributeMap();
				}

				public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
				}
			};
		}
		throw new FlowArtifactException(id, FlowAttributeMapper.class);
	}
	
	public class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			if (context.getFlowExecutionContext().getFlow().getAttributeMap().contains("scenario2")) {
				return new Event(this, "event2");
			}
			return new Event(this, "event1");
		}
	}
	
	public class TestMultiAction extends MultiAction {
		public Event actionMethod(RequestContext context) throws Exception {
			throw new MyCustomException("Oops!");
		}
	}

	public class TestPojo {
		public boolean booleanMethod() {
			return true;
		}

		public LabeledEnum enumMethod() {
			return FlowSessionStatus.CREATED;
		}
	}
	
	public class TestAttributeMapper implements FlowAttributeMapper {
		public AttributeMap createSubflowInput(RequestContext context) {
			return new AttributeMap();
		}

		public void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		}
	}
}