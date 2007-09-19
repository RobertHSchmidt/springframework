package org.springframework.webflow.engine.builder;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.AbstractFlowBuilder;
import org.springframework.webflow.util.ResourceHolder;

public class RefreshableFlowDefinitionHolderTests extends TestCase {
	private RefreshableFlowDefinitionHolder holder;
	private FlowAssembler assembler;

	protected void setUp() {
		FlowAssembler assembler = new FlowAssembler("flowId", new SimpleFlowBuilder(), null);
		holder = new RefreshableFlowDefinitionHolder(assembler);
	}

	public void testGetFlowDefinition() {
		FlowDefinition flow = holder.getFlowDefinition();
		assertEquals("flowId", flow.getId());
		assertEquals("end", flow.getStartState().getId());
	}

	public void testGetFlowDefinitionWithChangesRefreshed() {
		assembler = new FlowAssembler("flowId", new ChangeDetectableFlowBuilder(), null);
		holder = new RefreshableFlowDefinitionHolder(assembler);
		FlowDefinition flow = holder.getFlowDefinition();
		flow = holder.getFlowDefinition();
		assertEquals("flowId", flow.getId());
		assertEquals("end", flow.getStartState().getId());
	}

	public class SimpleFlowBuilder extends AbstractFlowBuilder implements FlowBuilder {

		public void buildStates() throws FlowBuilderException {
			new EndState(getFlow(), "end");
		}

		public void init(String flowId, AttributeMap attributes) throws FlowBuilderException {
			setFlow(Flow.create(flowId, attributes));
		}
	}

	public class ChangeDetectableFlowBuilder extends SimpleFlowBuilder implements ResourceHolder {
		private FileSystemResource resource = new FileSystemResource("file.txt");

		public Resource getResource() {
			return resource;
		}
	}

}
