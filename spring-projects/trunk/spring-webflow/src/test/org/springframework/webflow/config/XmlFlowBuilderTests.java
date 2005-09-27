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
package org.springframework.webflow.config;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.access.FlowArtifactLookupException;
import org.springframework.webflow.support.SimpleViewDescriptorCreator;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for XML flow builder.
 * 
 * @see org.springframework.webflow.config.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilderTests extends TestCase {

	private Flow flow;

	private MockRequestContext context;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTests.class));
		builder.setFlowArtifactLocator(new TestFlowArtifactLocator());
		flow = new FlowFactoryBean(builder).getFlow();
		context = new MockRequestContext();
	}

	private Event createEvent(String id) {
		return new Event(this, id);
	}

	public void testBuildResult() {
		assertNotNull(flow);
		assertEquals("testFlow", flow.getId());
		assertEquals("actionState1", flow.getStartState().getId());
		assertEquals(7, flow.getStateIds().length);

		ActionState actionState1 = (ActionState)flow.getState("actionState1");
		assertNotNull(actionState1);
		assertEquals(2, actionState1.getActionCount());
		assertEquals(null, actionState1.getAnnotatedAction().getCaption());
		assertEquals(Boolean.TRUE, (Boolean)actionState1.getProperty("propBoolean"));
		assertEquals("aString", actionState1.getProperty("propString"));
		assertEquals("action2Name", actionState1.getAnnotatedActions()[1].getName());
		assertEquals(2, actionState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(actionState1.hasTransitionFor(context));
		Transition transition = actionState1.getRequiredTransition(context);
		assertEquals("viewState1", transition.getTargetStateId());
		context.setLastEvent(createEvent("action2Name.event2"));
		assertTrue(actionState1.hasTransitionFor(context));
		transition = actionState1.getRequiredTransition(context);
		assertEquals("viewState2", transition.getTargetStateId());
		assertEquals("prop1Value", actionState1.getAnnotatedActions()[0].getAttribute("prop1"));
		assertEquals("prop2Value", actionState1.getAnnotatedActions()[0].getAttribute("prop2"));

		ViewState viewState1 = (ViewState)flow.getState("viewState1");
		assertNotNull(viewState1);
		assertFalse(viewState1.isMarker());
		assertEquals("view1", ((SimpleViewDescriptorCreator)viewState1.getViewDescriptorCreator()).getViewName());
		assertEquals(1, viewState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(viewState1.hasTransitionFor(context));
		transition = viewState1.getRequiredTransition(context);
		assertEquals("subFlowState1", transition.getTargetStateId());

		ViewState viewState2 = (ViewState)flow.getState("viewState2");
		assertNotNull(viewState2);
		assertTrue(viewState2.isMarker());
		assertNull(viewState2.getViewDescriptorCreator());
		assertEquals(1, viewState2.getTransitions().length);
		context.setLastEvent(createEvent("event2"));
		assertTrue(viewState2.hasTransitionFor(context));
		transition = viewState2.getRequiredTransition(context);
		assertEquals("subFlowState2", transition.getTargetStateId());

		SubflowState subFlowState1 = (SubflowState)flow.getState("subFlowState1");
		assertNotNull(subFlowState1);
		assertNotNull(subFlowState1.getSubflow());
		assertEquals("subFlow1", subFlowState1.getSubflow().getId());
		assertNotNull(subFlowState1.getAttributeMapper());
		assertEquals(1, subFlowState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(subFlowState1.hasTransitionFor(context));
		transition = subFlowState1.getRequiredTransition(context);
		assertEquals("endState1", transition.getTargetStateId());

		SubflowState subFlowState2 = (SubflowState)flow.getState("subFlowState2");
		assertNotNull(subFlowState2);
		assertNotNull(subFlowState2.getSubflow());
		assertEquals("subFlow2", subFlowState2.getSubflow().getId());
		assertNotNull(subFlowState2.getAttributeMapper());
		assertEquals(1, subFlowState2.getTransitions().length);
		context.setLastEvent(createEvent("event2"));
		assertTrue(subFlowState2.hasTransitionFor(context));
		transition = subFlowState2.getRequiredTransition(context);
		assertEquals("endState2", transition.getTargetStateId());

		EndState endState1 = (EndState)flow.getState("endState1");
		assertNotNull(endState1);
		assertFalse(endState1.isMarker());
		assertEquals("endView1", ((SimpleViewDescriptorCreator)endState1.getViewDescriptorCreator()).getViewName());

		EndState endState2 = (EndState)flow.getState("endState2");
		assertNotNull(endState2);
		assertTrue(endState2.isMarker());
		assertNull(endState2.getViewDescriptorCreator());

	}

	/**
	 * Flow service locator for the services needed by the testFlow (defined in
	 * testFlow.xml)
	 * 
	 * @author Erwin Vervaet
	 */
	public static class TestFlowArtifactLocator implements FlowArtifactLocator {

		public Action getAction(String id) throws FlowArtifactLookupException {
			if ("action1".equals(id) || "action2".equals(id)) {
				return new TestAction();
			}
			throw new FlowArtifactLookupException(Action.class, id);
		}

		public Flow getFlow(String id) throws FlowArtifactLookupException {
			if ("subFlow1".equals(id) || "subFlow2".equals(id)) {
				return new Flow(id);
			}
			throw new FlowArtifactLookupException(Flow.class, id);
		}

		public FlowAttributeMapper getFlowAttributeMapper(String id) throws FlowArtifactLookupException {
			if ("attributeMapper1".equals(id)) {
				return new FlowAttributeMapper() {
					public Map createSubflowInput(RequestContext context) {
						return new HashMap();
					}

					public void mapSubflowOutput(RequestContext context) {
					}
				};
			}
			throw new FlowArtifactLookupException(FlowAttributeMapper.class, id);
		}
	};

	public static class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			return new Event(this, "event1");
		}
	}
}