package org.springframework.webflow.engine.builder;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.webflow.core.collection.CollectionUtils;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;

public class FlowAssemblerTests extends TestCase {
	private FlowBuilder builder;
	private FlowAssembler assembler;

	protected void setUp() {
		builder = (FlowBuilder) EasyMock.createMock(FlowBuilder.class);
		assembler = new FlowAssembler("search", builder);
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
		EasyMock.expect(builder.getFlow()).andReturn(new Flow("search"));
		EasyMock.replay(new Object[] { builder });
		Flow flow = assembler.assembleFlow();
		assertEquals("search", flow.getId());
		EasyMock.verify(new Object[] { builder });
	}

	public void testAssembleFlowWithAttributes() {
		MutableAttributeMap map = new LocalAttributeMap();
		map.put("foo", "bar");
		assembler = new FlowAssembler("search", builder, map);
		builder.init("search", map);
		builder.buildVariables();
		builder.buildInputMapper();
		builder.buildStartActions();
		builder.buildInlineFlows();
		builder.buildStates();
		builder.buildGlobalTransitions();
		builder.buildEndActions();
		builder.buildOutputMapper();
		builder.buildExceptionHandlers();
		builder.dispose();
		Flow flow = new Flow("search");
		flow.getAttributeMap().putAll(map);
		EasyMock.expect(builder.getFlow()).andReturn(flow);
		EasyMock.replay(new Object[] { builder });
		flow = assembler.assembleFlow();
		assertEquals("search", flow.getId());
		assertEquals("bar", flow.getAttributeMap().get("foo"));
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
