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

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.webflow.execution.FlowExecution;

/**
 * TODO doc
 * 
 * @author Colin Sampaleanu
 * @since 1.0
 */
public class FlowVariableResolver extends VariableResolver {

	/**
	 * Name of the exposed flow scope variable: "flow".
	 */
	public static final String FLOW_VARIABLE_NAME = "flow";

	protected final Log logger = LogFactory.getLog(getClass());

	protected final VariableResolver originalVariableResolver;

	/**
	 * Create a new FlowVariableResolver, using the given original
	 * VariableResolver.
	 * <p>
	 * A JSF implementation will automatically pass its original resolver into
	 * the constructor of a configured resolver, provided that there is a
	 * corresponding constructor argument.
	 * 
	 * @param originalVariableResolver the original VariableResolver
	 */
	public FlowVariableResolver(VariableResolver originalVariableResolver) {
		this.originalVariableResolver = originalVariableResolver;
	}

	/**
	 * Return the original VariableResolver that this resolver delegates to.
	 */
	protected final VariableResolver getOriginalVariableResolver() {
		return originalVariableResolver;
	}

	/**
	 * Check for the special "flow" variable first, then delegate to the
	 * original VariableResolver.
	 */
	public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
		if (!FLOW_VARIABLE_NAME.equals(name)) {
			return this.originalVariableResolver.resolveVariable(context, name);
		}
		FlowExecution execution = FlowExecutionHolder.getFlowExecution();
		if (execution == null)
			throw new EvaluationException(
					"'flow' variable prefix specified, but a FlowExecution is not bound to thread context as it should be");
		return execution;
	}
}