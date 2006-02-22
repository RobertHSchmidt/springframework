/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.attribute.AttributeMap;
import org.springframework.binding.attribute.UnmodifiableAttributeMap;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.binding.method.MethodKey;
import org.springframework.webflow.Event;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for ParameterizableFlowAttributeMapper.
 * 
 * @author Erwin Vervaet
 */
public class DefaultFlowAttributeMapperTests extends TestCase {

	private DefaultFlowAttributeMapper mapper;

	private MockRequestContext context;

	private MockFlowSession parentSession;

	private MockFlowSession subflowSession;

	private MappingBuilder mapping;

	protected void setUp() throws Exception {
		mapper = new DefaultFlowAttributeMapper();
		mapping = new MappingBuilder();
		context = new MockRequestContext();
		parentSession = new MockFlowSession();
		subflowSession = new MockFlowSession();
		subflowSession.setParent(parentSession);
	}

	public void testDirectMapping() {
		mapper.addInputMapping(mapping.source("${flowScope.x}").target("${y}").value());
		mapper.addOutputMapping(mapping.source("y").value());

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(1, input.getAttributeCount());
		assertEquals("xValue", input.getAttribute("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("y", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(1, parentSession.getScope().getAttributeCount());
		assertEquals("xValue", parentSession.getScope().getAttribute("y"));
	}

	public void testSimpleMapping() {
		mapper.addInputAttributes(new String[] { "someAttribute", "someOtherAttribute" });
		mapper.addOutputAttributes(new String[] { "someAttribute", "someOtherAttribute" });

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("someAttribute", "someValue");
		context.getFlowScope().setAttribute("someOtherAttribute", "someOtherValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertEquals("someValue", input.getAttribute("someAttribute"));
		assertEquals("someOtherValue", input.getAttribute("someOtherAttribute"));

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("someAttribute", "someUpdatedValue");
		subflowOutput.setAttribute("someOtherAttribute", "someOtherUpdatedValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertEquals("someUpdatedValue", parentSession.getScope().getAttribute("someAttribute"));
		assertEquals("someOtherUpdatedValue", parentSession.getScope().getAttribute("someOtherAttribute"));
	}

	public void testBeanPropertyMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("flowScope.bean.prop").target("attr").value(),
				mapping.source("flowScope.bean").target("otherBean").value(),
				mapping.source("flowScope.otherAttr").target("otherBean.prop ").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("bean.prop").target("attr").value(),
				mapping.source("bean").target("otherBean").value(),
				mapping.source("otherAttr").target("otherBean.prop").value() });

		TestBean bean = new TestBean();
		bean.setProp("value");

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("bean", bean);
		context.getFlowScope().setAttribute("otherAttr", "otherValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertEquals("value", input.getAttribute("attr"));
		assertEquals("otherValue", ((TestBean)input.getAttribute("otherBean")).getProp());

		parentSession.getScope().clear();
		bean.setProp("value");

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("bean", bean);
		subflowOutput.setAttribute("otherAttr", "otherValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertEquals("value", parentSession.getScope().getAttribute("attr"));
		assertEquals("otherValue", ((TestBean)parentSession.getScope().getAttribute("otherBean")).getProp());
	}

	public void testExpressionMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("${requestScope.a}").target("b").value(),
				mapping.source("${flowScope.x}").target("y").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("b").target("c").value(),
				mapping.source("y").target("z").value() });

		context.setActiveSession(parentSession);
		context.getRequestScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertEquals("aValue", input.getAttribute("b"));
		assertEquals("xValue", input.getAttribute("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("b", "aValue");
		subflowOutput.setAttribute("y", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertEquals("aValue", parentSession.getScope().getAttribute("c"));
		assertEquals("xValue", parentSession.getScope().getAttribute("z"));
	}

	public void testNullMapping() {
		mapper.addInputMappings(new Mapping[] { mapping.source("${flowScope.x}").target("y").value(),
				mapping.source("${flowScope.a}").target("b").value() });
		mapper.addOutputMappings(new Mapping[] { mapping.source("y").target("c").value(),
				mapping.source("b").target("z").value() });

		parentSession.getScope().setAttribute("x", null);

		context.setActiveSession(parentSession);
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertTrue(input.containsAttribute("y"));
		assertNull(input.getAttribute("y"));
		assertTrue(input.containsAttribute("b"));
		assertNull(input.getAttribute("b"));

		parentSession.getScope().clear();

		mapper.mapSubflowOutput(UnmodifiableAttributeMap.EMPTY_MAP, context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertTrue(parentSession.getScope().containsAttribute("c"));
		assertNull(parentSession.getScope().getAttribute("c"));
		assertTrue(parentSession.getScope().containsAttribute("z"));
		assertNull(parentSession.getScope().getAttribute("z"));
	}

	public void testFormActionInCombinationWithMapping() throws Exception {
		context.setLastEvent(new Event(this, "start"));

		context.setActiveSession(parentSession);
		assertTrue(context.getFlowScope().getAttributeCount() == 0);

		FormAction action = new FormAction();
		action.setFormObjectName("command");
		action.setFormObjectClass(TestBean.class);
		action.setFormObjectScope(ScopeType.FLOW);
		context.setAttribute("method", new MethodKey("setupForm"));

		action.execute(context);

		assertEquals(2, context.getFlowScope().getAttributeCount());
		assertNotNull(context.getFlowScope().getAttribute("command"));

		mapper.addInputMapping(mapping.source("${flowScope.command}").target("command").value());
		AttributeMap input = mapper.createSubflowInput(context);

		assertEquals(1, input.getAttributeCount());
		assertSame(parentSession.getScope().getAttribute("command"), input.getAttribute("command"));
		assertTrue(subflowSession.getScope().getAttributeCount() == 0);
		subflowSession.getScope().replaceWith(input);

		context.setActiveSession(subflowSession);
		assertEquals(1, context.getFlowScope().getAttributeCount());

		action.execute(context);

		assertEquals(2, context.getFlowScope().getAttributeCount());
		assertSame(parentSession.getScope().getAttribute("command"), context.getFlowScope().getAttribute("command"));
	}

	public static class TestBean {
		private String prop;

		public String getProp() {
			return prop;
		}

		public void setProp(String prop) {
			this.prop = prop;
		}
	}
}