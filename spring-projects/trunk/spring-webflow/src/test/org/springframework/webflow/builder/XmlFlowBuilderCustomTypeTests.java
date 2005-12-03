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
package org.springframework.webflow.builder;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionControlContext;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.Transition;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewState;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.support.ParameterizableFlowAttributeMapper;

/**
 * Test case for XML flow builder, testing pluggability of custom types.
 * 
 * @see org.springframework.webflow.builder.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilderCustomTypeTests extends TestCase {

	private Flow flow;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow3.xml",
				XmlFlowBuilderCustomTypeTests.class), new FlowArtifactFactoryAdapter());
		FlowAssembler assembler = new FlowAssembler("testFlow3", builder);
		assembler.assembleFlow();
		flow = builder.getResult();
	}

	public void testBuildResult() {
		assertEquals("testFlow3", flow.getId());
		assertEquals(5, flow.getStateCount());
		assertEquals(1, flow.getExceptionHandlers().length);

		assertSame(flow.getClass(), CustomFlow.class);
		assertSame(flow.getState("actionState1").getClass(), CustomActionState.class);
		assertSame(((ActionState)flow.getState("actionState1")).getAnnotatedAction().getTargetAction().getClass(),
				CustomAction.class);
		assertSame(flow.getState("subFlowState1").getClass(), CustomSubflowState.class);
		assertSame(((SubflowState)flow.getState("subFlowState1")).getAttributeMapper().getClass(),
				CustomAttributeMapper.class);
		assertSame(flow.getState("viewState1").getClass(), CustomViewState.class);
		assertSame(((ViewState)flow.getState("viewState1")).getTransitions()[0].getClass(), CustomTransition.class);
		assertSame(flow.getState("decisionState1").getClass(), CustomDecisionState.class);
		assertSame(flow.getState("endState1").getClass(), CustomEndState.class);
		assertSame(flow.getExceptionHandlers()[0].getClass(), CustomExceptionHandler.class);
	}

	public static class CustomFlow extends Flow {
	}

	public static class CustomActionState extends ActionState {
	}

	public static class CustomAction extends AbstractAction {
		protected Event doExecute(RequestContext context) throws Exception {
			return success();
		}
	}

	public static class CustomSubflowState extends SubflowState {
	}

	public static class CustomAttributeMapper extends ParameterizableFlowAttributeMapper {
	}

	public static class CustomViewState extends ViewState {
	}

	public static class CustomTransition extends Transition {
	}

	public static class CustomDecisionState extends DecisionState {
	}

	public static class CustomEndState extends EndState {
	}

	public static class CustomExceptionHandler implements StateExceptionHandler {
		public boolean handles(StateException exception) {
			return false;
		}

		public ViewSelection handle(StateException exception, FlowExecutionControlContext context) {
			return null;
		}
	}
}