/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Converter that takes an encoded string representation and produces
 * a corresponding <code>TransitionCriteria</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in a TransitionCriteria object that matches on everything
 * ({@link org.springframework.webflow.TransitionCriteriaFactory#alwaysTrue()})
 * </li>
 * <li>"eventId" - will result in a TransitionCriteria object that matches given
 * event id ({@link org.springframework.webflow.TransitionCriteriaFactory#eventId(String)})
 * </li>
 * <li>"${...}" - will result in a TransitionCriteria object that evaluates given
 * condition, expressed as an expression
 * </li>
 * <li>"class:&lt;classname&gt;" - will result in instantiation and usage of a custom 
 * TransitionCriteria implementation. The implementation must have a public no-arg constructor.
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * @see org.springframework.webflow.TransitionCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends ConversionServiceAwareConverter {
	
	/**
	 * Create a new converter that converts strings to transition
	 * criteria objects.
	 */
	public TextToTransitionCriteria() {
	}

	/**
	 * Create a new converter that converts strings to transition
	 * criteria objects. The given conversion service will be used to do
	 * all necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTransitionCriteria(ConversionService conversionService) {
		super(conversionService);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class } ;
	}
	
	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria) ||
				TransitionCriteriaFactory.WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return TransitionCriteriaFactory.alwaysTrue();
		}
		else if (encodedCriteria.startsWith(CLASS_PREFIX)) {
			Object o = newInstance(encodedCriteria);
			Assert.isInstanceOf(TransitionCriteria.class, o, "Encoded criteria class is of wrong type: ");
			return (TransitionCriteria)o;
		}
		else {
			return createBooleanExpressionTransitionCriteria(encodedCriteria);
		}
	}

	/**
	 * Factory method overridable by subclasses to customize expression-based
	 * transition criteria.
	 * @param expressionString the expression
	 * @return the criteria
	 * @throws ConversionException when there is a problem parsing the expression
	 */
	protected TransitionCriteria createBooleanExpressionTransitionCriteria(String expressionString) throws ConversionException {
		Expression expression = (Expression)fromStringTo(Expression.class).execute(expressionString);
		if (expression instanceof StaticExpression) {
			return TransitionCriteriaFactory.eventId(expressionString);
		}
		else {
			return new BooleanExpressionTransitionCriteria(expression);
		}
	}
	
	/**
	 * Transition criteria that tests the value of an expression. The
	 * expression is used to express a condition that guards transition
	 * execution in a web flow.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public static class BooleanExpressionTransitionCriteria implements TransitionCriteria {

		private static final String RESULT_ALIAS = "result";
		
		/**
		 * The expression evaluator to use.
		 */
		private Expression booleanExpression;

		/**
		 * Create a new expression based transition criteria object.
		 * @param expression the expression evaluator testing the criteria,
		 *        this expression should be a condition that returns a Boolean value
		 */
		public BooleanExpressionTransitionCriteria(Expression expression) {
			this.booleanExpression = expression;
		}

		public boolean test(RequestContext context) {
			Object result = this.booleanExpression.evaluateAgainst(context, getEvaluationContext(context));
			Assert.isInstanceOf(Boolean.class, result, "Impossible to determine result of boolean expression: ");
			return ((Boolean)result).booleanValue();
		}

		/**
		 * Setup a map with a few aliased values to make writing expression based
		 * transition conditions a bit easier.
		 */
		protected Map getEvaluationContext(RequestContext context) {
			Map evalContext = new HashMap();
			// ${#result == lastEvent.id}
			if (context.getLastEvent() != null) {
				evalContext.put(RESULT_ALIAS, context.getLastEvent().getId());
			}
			return evalContext;
		}

		public String toString() {
			return booleanExpression.toString();
		}
	}
}