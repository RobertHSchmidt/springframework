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
package org.springframework.webflow.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.convert.support.TextToClass;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionCriteriaFactory;

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
 * TransitionCriteria implementation.  The implementation must have a public no-arg constructor.
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * @see org.springframework.webflow.TransitionCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends AbstractConverter {

	/**
	 * Prefix used when the encoded transition criteria denotes a custom
	 * TransitionCriteria implementation.
	 */
	public static final String CLASS_PREFIX = "class:";

	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();
	
	/**
	 * Returns the expression parser used by this converter.
	 */
	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}
	
	/**
	 * Set the expression parser used by this converter.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}
	
	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class } ;
	}
	
	protected Object doConvert(Object source, Class targetClass) throws ConversionException {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria) || TransitionCriteriaFactory.WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return TransitionCriteriaFactory.alwaysTrue();
		}
		else if (expressionParser.isExpression(encodedCriteria)) {
			return createExpressionTransitionCriteria(encodedCriteria);
		}
		else if (encodedCriteria.startsWith(CLASS_PREFIX)) {
			String className = encodedCriteria.substring(CLASS_PREFIX.length());
			Class clazz = (Class)new TextToClass().convert(className);
			Object o = BeanUtils.instantiateClass(clazz);
			Assert.isInstanceOf(TransitionCriteria.class, o, "Encoded criteria class is of wrong type:");
			return (TransitionCriteria)o;
		}
		else {
			return TransitionCriteriaFactory.eventId(encodedCriteria);
		}
	}

	/**
	 * Factory method overridable by subclasses to customize expression-based
	 * transition criteria.
	 * @param expression the expression
	 * @return the criteria
	 * @throws ConversionException when there is a problem parsing the expression
	 */
	protected TransitionCriteria createExpressionTransitionCriteria(String expression) throws ConversionException {
		try {
			return new ExpressionTransitionCriteria(expressionParser.parseExpression(expression));
		}
		catch (ParserException e) {
			throw new ConversionException(expression, ExpressionTransitionCriteria.class, e);
		}
	}
	
	/**
	 * Transtition criteria that tests the value of an expression. The
	 * expression is used to express a condition that guards transition
	 * execution in a web flow.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 * @author Rob Harrop
	 */
	public static class ExpressionTransitionCriteria implements TransitionCriteria {

		/**
		 * The expression evaluator to use.
		 */
		private Expression evaluator;

		/**
		 * Create a new expression based transition criteria object.
		 * @param evaluator the expression evaluator testing the criteria,
		 *        this expression should be a condition that returns a Boolean value
		 */
		public ExpressionTransitionCriteria(Expression evaluator) {
			this.evaluator = evaluator;
		}

		public boolean test(RequestContext context) {
			Object result = this.evaluator.evaluateAgainst(context, getEvaluationContext(context));
			Assert.isInstanceOf(Boolean.class, result);
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
				evalContext.put("result", context.getLastEvent().getId());
			}
			return evalContext;
		}

		public String toString() {
			return evaluator.toString();
		}
	}	
}