package org.springframework.webflow.builder;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.enums.LabeledEnum;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.registry.NoSuchFlowDefinitionException;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowAttributeMapper;
import org.springframework.webflow.engine.builder.BaseFlowServiceLocator;
import org.springframework.webflow.engine.builder.FlowArtifactLookupException;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowSessionStatus;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.support.UnmodifiableAttributeMap;

/**
 * Flow service locator for the services needed by the testFlow (defined in
 * testFlow.xml)
 * 
 * @author Erwin Vervaet
 */
public class TestFlowArtifactFactory extends BaseFlowServiceLocator {

	public StaticListableBeanFactory registry = new StaticListableBeanFactory();
	
	public TestFlowArtifactFactory() {
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
		throw new NoSuchFlowDefinitionException(id, new String[] {"subFlow1", "subFlow2" });
	}

	public class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			if (context.getFlowExecutionContext().getFlowDefinition().getAttributeMap().contains("scenario2")) {
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
		public LocalAttributeMap createFlowInput(RequestContext context) {
			return new LocalAttributeMap();
		}

		public void mapFlowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		}
	}

	public BeanFactory getBeanFactory() throws UnsupportedOperationException {
		return registry;
	}
	
}