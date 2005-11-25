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
package org.springframework.webflow.config.support;

import java.util.Collections;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.WildcardTransitionCriteria;

/**
 * Converter that takes an encoded string representation and produces a
 * corresponding <code>TransitionCriteria</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"*" - will result in a TransitionCriteria object that matches on
 * everything ({@link org.springframework.webflow.WildcardTransitionCriteria})
 * </li>
 * <li>"eventId" - will result in a TransitionCriteria object that matches
 * given event id ({@link org.springframework.webflow.config.support.EventIdTransitionCriteria})
 * </li>
 * <li>"${...}" - will result in a TransitionCriteria object that evaluates
 * given condition, expressed as an expression ({@link org.springframework.webflow.config.support.BooleanExpressionTransitionCriteria})
 * </li>
 * </ul>
 * 
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends AbstractConverter {

	/**
	 * Parser to user for parsing transition criteria expressions.
	 */
	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();

	/**
	 * Create a new converter that converts strings to transition criteria
	 * objects. The given conversion service will be used to do all necessary
	 * internal conversion (e.g. parsing expression strings).
	 */
	public TextToTransitionCriteria() {
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedCriteria = (String)source;
		if (!StringUtils.hasText(encodedCriteria)
				|| WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return WildcardTransitionCriteria.INSTANCE;
		}
		else if (expressionParser.isExpression(encodedCriteria)) {
			Expression expression = (Expression)expressionParser
					.parseExpression(encodedCriteria, Collections.EMPTY_MAP);
			return createBooleanExpressionTransitionCriteria(expression);
		}
		else {
			return createEventIdTransitionCriteria(encodedCriteria);
		}
	}

	/**
	 * Hook method subclasses can override to return a specialized eventId
	 * matching transition criteria implementation.
	 * @param eventId the event id to match
	 * @return the transition criteria object
	 * @throws ConversionException when something goes wrong
	 */
	protected TransitionCriteria createEventIdTransitionCriteria(String eventId) throws ConversionException {
		return new EventIdTransitionCriteria(eventId);
	}

	/**
	 * Hook method subclasses can override to return a specialized expression
	 * evaluating transition criteria implementation.
	 * @param expression the expression to evaluate
	 * @return the transition criteria object
	 * @throws ConversionException when something goes wrong
	 */
	protected TransitionCriteria createBooleanExpressionTransitionCriteria(Expression expression)
			throws ConversionException {
		return new BooleanExpressionTransitionCriteria(expression);
	}
}