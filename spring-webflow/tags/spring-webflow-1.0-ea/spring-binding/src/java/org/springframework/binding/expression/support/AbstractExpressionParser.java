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
package org.springframework.binding.expression.support;

import java.util.LinkedList;
import java.util.List;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.util.StringUtils;

/**
 * An expression parser that parses Ognl expressions.
 * @author Keith
 */
public abstract class AbstractExpressionParser implements ExpressionParser {

	/**
	 * The expression prefix.
	 */
	private static final String DEFAULT_EXPRESSION_PREFIX = "${";

	/**
	 * The expression suffix.
	 */
	private static final String DEFAULT_EXPRESSION_SUFFIX = "}";

	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	public boolean isExpression(String encodedCriteria) {
		return (encodedCriteria.startsWith(getExpressionPrefix()) && encodedCriteria.endsWith(getExpressionSuffix()));
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 */
	protected String cutExpression(String encodedCriteria) {
		if (isExpression(encodedCriteria)) {
			return encodedCriteria.substring(DEFAULT_EXPRESSION_PREFIX.length(), encodedCriteria.length()
					- DEFAULT_EXPRESSION_SUFFIX.length());
		}
		else {
			return encodedCriteria;
		}
	}

	protected String getExpressionPrefix() {
		return DEFAULT_EXPRESSION_PREFIX;
	}

	protected String getExpressionSuffix() {
		return DEFAULT_EXPRESSION_SUFFIX;
	}

	/*
	 * Helper that parses given expression string using the configured parser.
	 * The expression string can contain any number of expressions, all
	 * contained in "${...}" markers. For instance: "foo${expr0}bar${expr1}".
	 * The static pieces of text will also be returned as Expressions that just
	 * return that static piece of text. As a result, evaluating all returned
	 * expressions and concating the results produces the complete evaluated
	 * string. @param expressionString the expression string @return the parsed
	 * expressions @throws ParserException when the expressions cannot be parsed
	 */
	public Expression[] parseExpressions(String expressionString) throws ParserException {
		List expressions = new LinkedList();
		if (StringUtils.hasText(expressionString)) {
			int startIdx = 0;
			while (startIdx < expressionString.length()) {
				int exprStartIdx = expressionString.indexOf(getExpressionPrefix(), startIdx);
				if (exprStartIdx >= startIdx) {
					// an expression was found
					if (exprStartIdx > startIdx) {
						expressions.add(new StaticExpression(expressionString.substring(startIdx, exprStartIdx)));
						startIdx = exprStartIdx;
					}
					int exprEndIdx = expressionString.indexOf(getExpressionSuffix(), exprStartIdx);
					if (exprEndIdx >= exprStartIdx) {
						expressions.add(parseExpression(expressionString.substring(exprStartIdx, exprEndIdx + 1)));
						startIdx = exprEndIdx + 1;
					}
					else {
						expressions.add(new StaticExpression(expressionString.substring(startIdx)));
						startIdx = expressionString.length();
					}
				}
				else {
					// no expression could be found
					expressions.add(new StaticExpression(expressionString.substring(startIdx)));
					startIdx = expressionString.length();
				}
			}
		}
		return (Expression[])expressions.toArray(new Expression[expressions.size()]);
	}

	public abstract Expression parseExpression(String expressionString) throws ParserException;

	public abstract PropertyExpression parsePropertyExpression(String expressionString) throws ParserException, UnsupportedOperationException;
	
}