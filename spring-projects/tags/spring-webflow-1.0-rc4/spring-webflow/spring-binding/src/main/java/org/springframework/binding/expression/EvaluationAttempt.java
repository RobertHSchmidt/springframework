/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.binding.expression;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * A simple holder for information about an evaluation attempt.
 * 
 * @author Keith
 */
public class EvaluationAttempt implements Serializable {

	/**
	 * The expression being evaluated.
	 */
	private Expression expression;

	/**
	 * The target object being evaluated on.
	 */
	private Object target;

	/**
	 * The evaluation context.
	 */
	private Map evaluationContext;

	/**
	 * Create an evaluation attempt.
	 * 
	 * @param expression
	 * @param target
	 * @param evaluationContext
	 */
	public EvaluationAttempt(Expression expression, Object target, Map evaluationContext) {
		this.expression = expression;
		this.target = target;
		this.evaluationContext = evaluationContext;
	}

	public Expression getExpression() {
		return expression;
	}

	public Object getTarget() {
		return target;
	}

	public Map getEvaluationContext() {
		return evaluationContext;
	}

	public String toString() {
		return createToString(new ToStringCreator(this)).toString();
	}

	protected ToStringCreator createToString(ToStringCreator creator) {
		return creator.append("expression", expression).append("target", target).append("context", evaluationContext);
	}
}