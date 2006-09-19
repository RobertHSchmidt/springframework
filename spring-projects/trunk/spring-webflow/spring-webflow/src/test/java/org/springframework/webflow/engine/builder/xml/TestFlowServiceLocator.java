package org.springframework.webflow.engine.builder.xml;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.webflow.TestException;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.engine.builder.BaseFlowServiceLocator;
import org.springframework.webflow.engine.builder.FlowArtifactLookupException;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Flow service locator for the services needed by the testFlow (defined in
 * testFlow.xml)
 * 
 * @author Erwin Vervaet
 */
public class TestFlowServiceLocator extends BaseFlowServiceLocator {

	public StaticListableBeanFactory registry = new StaticListableBeanFactory();

	public TestFlowServiceLocator() {
		init();
	}

	public void init() {
		registry.addBean("action1", new TestAction());
		registry.addBean("action2", new TestAction());
		registry.addBean("multiAction", new TestMultiAction());
		registry.addBean("pojoAction", new TestPojo());
		registry.addBean("attributeMapper1", new TestAttributeMapper());
	}

	public Flow getSubflow(String id) throws FlowArtifactLookupException {
		if ("subFlow1".equals(id) || "subFlow2".equals(id)) {
			Flow flow = new Flow(id);
			new EndState(flow, "finish");
			return flow;
		}
		throw new NoSuchFlowDefinitionException(id, new String[] { "subFlow1", "subFlow2" });
	}

	public class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			if (context.getFlowExecutionContext().getDefinition().getAttributes().contains("scenario2")) {
				return new Event(this, "event2");
			}
			return new Event(this, "event1");
		}
	}

	public class TestMultiAction extends MultiAction {
		public Event actionMethod(RequestContext context) throws Exception {
			throw new TestException("Oops!");
		}
	}

	public class TestAttributeMapper implements FlowAttributeMapper {
		public MutableAttributeMap createFlowInput(RequestContext context) {
			return new LocalAttributeMap();
		}

		public void mapFlowOutput(AttributeMap subflowOutput, RequestContext context) {
		}
	}

	public BeanFactory getBeanFactory() throws UnsupportedOperationException {
		return registry;
	}

}