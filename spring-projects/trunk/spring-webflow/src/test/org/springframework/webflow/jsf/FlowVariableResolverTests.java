/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.webflow.jsf;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.webflow.execution.FlowExecution;

/**
 * @author Colin Sampaleanu
 * @since 1.0
 */
public class FlowVariableResolverTests extends TestCase {

	public void testResolver() {

		FlowVariableResolver resolver = new FlowVariableResolver(
				new OriginalVariableResolver());

		assertEquals("val1", resolver.resolveVariable(null, "var1"));
		
		try {
			resolver.resolveVariable(null, "flow");
			fail("resolver can not work with no FlowExecution in thread local");
		} catch (EvaluationException e) { 
			// expected
		}
		
		MockControl flowExControl = MockControl.createControl(FlowExecution.class);
		Serializable flowExecutionId = "myId";
		FlowExecution flowEx = (FlowExecution) flowExControl.getMock();
		FlowExecutionHolder.setFlowExecution(flowExecutionId, flowEx, null, false);
		assertEquals(flowEx, resolver.resolveVariable(null, "flow"));
		
		FlowExecutionHolder.clearFlowExecution();
	}

	private static class OriginalVariableResolver extends VariableResolver {

		public Object resolveVariable(FacesContext facesContext, String name)
				throws EvaluationException {
			if ("var1".equals(name))
				return "val1";
			return null;
		}
	}

}
