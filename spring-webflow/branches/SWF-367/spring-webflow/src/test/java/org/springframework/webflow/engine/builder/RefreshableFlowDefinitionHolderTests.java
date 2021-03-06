package org.springframework.webflow.engine.builder;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.AbstractFlowBuilder;
import org.springframework.webflow.test.MockFlowBuilderContext;
import org.springframework.webflow.util.ResourceHolder;

public class RefreshableFlowDefinitionHolderTests extends TestCase {
	private RefreshableFlowDefinitionHolder holder;
	private FlowAssembler assembler;

	protected void setUp() {
		FlowAssembler assembler = new FlowAssembler(new SimpleFlowBuilder(), new MockFlowBuilderContext("flowId"));
		holder = new RefreshableFlowDefinitionHolder(assembler);
	}

	public void testGetFlowDefinition() {
		FlowDefinition flow = holder.getFlowDefinition();
		assertEquals("flowId", flow.getId());
		assertEquals("end", flow.getStartState().getId());
	}

	public void testGetFlowDefinitionWithChangesRefreshed() {
		assembler = new FlowAssembler(new ChangeDetectableFlowBuilder(), new MockFlowBuilderContext("flowId"));
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

		protected Flow createFlow() {
			return Flow.create(getContext().getFlowId(), getContext().getFlowAttributes());
		}

	}

	public class ChangeDetectableFlowBuilder extends SimpleFlowBuilder implements ResourceHolder {
		private FileSystemResource resource = new FileSystemResource("file.txt");

		public Resource getResource() {
			return resource;
		}
	}

}
