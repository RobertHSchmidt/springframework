package org.springframework.webflow.engine.builder;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.definition.FlowId;
import org.springframework.webflow.engine.Flow;

public class FlowAssemblerTests extends TestCase {
	private FlowBuilder builder;
	private FlowAssembler assembler;

	protected void setUp() {
		builder = (FlowBuilder) EasyMock.createMock(FlowBuilder.class);
		assembler = new FlowAssembler("search", builder, null);
	}

	public void testAssembleFlow() {
		builder.init("search", CollectionUtils.EMPTY_ATTRIBUTE_MAP);
		builder.dispose();
		builder.buildVariables();
		builder.buildInputMapper();
		builder.buildStartActions();
		builder.buildInlineFlows();
		builder.buildStates();
		builder.buildGlobalTransitions();
		builder.buildEndActions();
		builder.buildOutputMapper();
		builder.buildExceptionHandlers();
		EasyMock.expect(builder.getFlow()).andReturn("search");
		EasyMock.replay(new Object[] { builder });
		Flow flow = assembler.assembleFlow();
		assertEquals(FlowId.valueOf("search"), flow.getId());
		EasyMock.verify(new Object[] { builder });
	}

	public void testDisposeCalledOnException() {
		builder.init("search", CollectionUtils.EMPTY_ATTRIBUTE_MAP);
		EasyMock.expectLastCall().andThrow(new IllegalArgumentException());
		builder.dispose();
		EasyMock.replay(new Object[] { builder });
		try {
			assembler.assembleFlow();
			fail("Should have failed");
		} catch (IllegalArgumentException e) {
			EasyMock.verify(new Object[] { builder });
		}
	}
}
