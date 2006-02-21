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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.attribute.AttributeMap;
import org.springframework.binding.attribute.UnmodifiableAttributeMap;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.method.MethodKey;
import org.springframework.core.CollectionFactory;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import sun.reflect.generics.scope.Scope;

/**
 * Test case for ParameterizableFlowAttributeMapper.
 * 
 * @author Erwin Vervaet
 */
public class ParameterizableFlowAttributeMapperTests extends TestCase {

	private ParameterizableFlowAttributeMapper mapper;

	private MockRequestContext context;

	private MockFlowSession parentSession;

	private MockFlowSession subflowSession;

	protected void setUp() throws Exception {
		mapper = new ParameterizableFlowAttributeMapper();
		context = new MockRequestContext();
		parentSession = new MockFlowSession();
		subflowSession = new MockFlowSession();
		subflowSession.setParent(parentSession);
	}

	public void testDirectMapping() {
		Mapping testInputMapping = new Mapping("${flowScope.x}", "${y}");
		mapper.setInputMapping(testInputMapping);
		Mapping testOutputMapping = new Mapping("y");
		mapper.setOutputMapping(testOutputMapping);

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

	public void testDirectAttributeMapper() {
		AttributeMapper testInputMapper = new AttributeMapper() {
			public void map(Object source, Object target, Map context) {
				assertTrue(source instanceof RequestContext);
				assertTrue(target instanceof AttributeMap);
				((AttributeMap)target).setAttribute("y", ((RequestContext)source).getFlowScope().getAttribute("x"));
			}
		};
		mapper.setInputMapper(testInputMapper);
		AttributeMapper testOutputMapper = new AttributeMapper() {
			public void map(Object source, Object target, Map context) {
				assertTrue(source instanceof UnmodifiableAttributeMap);
				assertTrue(target instanceof AttributeMap);
				((AttributeMap)target).setAttribute("y", ((UnmodifiableAttributeMap)source).getAttribute("x"));
			}
		};
		mapper.setOutputMapper(testOutputMapper);

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(1, input.getAttributeCount());
		assertEquals("xValue", input.getAttribute("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("x", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(1, parentSession.getScope().getAttributeCount());
		assertEquals("xValue", parentSession.getScope().getAttribute("y"));
	}

	public void testSimpleMapping() {
		List mappings = new ArrayList();
		mappings.add("someAttribute");
		mappings.add("someOtherAttribute");
		mapper.setInputMappings(mappings);
		mapper.setOutputMappings(mappings);

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

	public void testMapMapping() {
		AttributeMap mappingsMap = new AttributeMap();
		mappingsMap.setAttribute("a", "b");
		mappingsMap.setAttribute("x", "y");
		mapper.setInputMappingsMap(mappingsMap.getMap());
		mapper.setOutputMappingsMap(mappingsMap.getMap());

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertEquals("aValue", input.getAttribute("b"));
		assertEquals("xValue", input.getAttribute("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("a", "aValue");
		subflowOutput.setAttribute("x", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertEquals("aValue", parentSession.getScope().getAttribute("b"));
		assertEquals("xValue", parentSession.getScope().getAttribute("y"));
	}

	public void testMixedMapping() {
		List mappings = new ArrayList();
		mappings.add("a");
		AttributeMap mappingsMap = new AttributeMap();
		mappingsMap.setAttribute("x", "y");
		mappings.add(mappingsMap);
		mapper.setInputMappings(mappings);
		mapper.setOutputMappings(mappings);

		context.setActiveSession(parentSession);
		context.getFlowScope().setAttribute("a", "aValue");
		context.getFlowScope().setAttribute("x", "xValue");
		AttributeMap input = mapper.createSubflowInput(context);
		assertEquals(2, input.getAttributeCount());
		assertEquals("aValue", input.getAttribute("a"));
		assertEquals("xValue", input.getAttribute("y"));

		parentSession.getScope().clear();

		AttributeMap subflowOutput = new AttributeMap();
		subflowOutput.setAttribute("a", "aValue");
		subflowOutput.setAttribute("x", "xValue");
		mapper.mapSubflowOutput(subflowOutput.unmodifiable(), context);
		assertEquals(2, parentSession.getScope().getAttributeCount());
		assertEquals("aValue", parentSession.getScope().getAttribute("a"));
		assertEquals("xValue", parentSession.getScope().getAttribute("y"));
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

	public void testBeanPropertyMapping() {
		AttributeMap mappingsMap = new AttributeMap();
		mappingsMap.setAttribute("bean.prop", "attr");
		mappingsMap.setAttribute("bean", "otherBean");
		mappingsMap.setAttribute("otherAttr", "otherBean.prop");
		mapper.setInputMappingsMap(mappingsMap.getMap());
		mapper.setOutputMappingsMap(mappingsMap.getMap());

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
		AttributeMap inputMappingsMap = new AttributeMap();
		inputMappingsMap.setAttribute("${requestScope.a}", "b");
		inputMappingsMap.setAttribute("${flowScope.x}", "y");
		mapper.setInputMappingsMap(inputMappingsMap.getMap());
		
		AttributeMap outputMappingsMap = new AttributeMap();
		outputMappingsMap.setAttribute("b", "c");
		outputMappingsMap.setAttribute("y", "z");
		mapper.setOutputMappingsMap(outputMappingsMap.getMap());

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
		AttributeMap mappingsMap = new AttributeMap();
		mappingsMap.setAttribute("${flowScope.x}", "y");
		mappingsMap.setAttribute("a", "b");
		mapper.setInputMappingsMap(mappingsMap.getMap());

		AttributeMap outputMappingsMap = new AttributeMap();
		outputMappingsMap.setAttribute("y", "c");
		outputMappingsMap.setAttribute("b", "z");
		mapper.setOutputMappingsMap(outputMappingsMap.getMap());

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

		AttributeMap mappingsMap = new AttributeMap();
		mappingsMap.setAttribute("${flowScope.command}", "command");
		mapper.setInputMappingsMap(mappingsMap.getMap());
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
}