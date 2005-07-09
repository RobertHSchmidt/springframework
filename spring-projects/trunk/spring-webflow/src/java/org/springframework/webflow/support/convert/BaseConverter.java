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
package org.springframework.webflow.support.convert;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.convert.support.TextToClass;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParserException;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for converters used by the web flow system. Provides
 * a number of convenience functionalities for use by subclasses, e.g. access
 * to a configurable expression parser.
 * 
 * @author Erwin Vervaet
 */
public abstract class BaseConverter extends AbstractConverter {

	/**
	 * Prefix used when the encoded representation denotes a custom
	 * object implementation.
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
	
	/**
	 * Helper that parses given expression string using the configured parser. The expression
	 * string can contain any number of expressions, all contained in "${...}" markers. For
	 * instance: "foo${expr0}bar${expr1}". The static pieces of text will also be returned
	 * as Expressions that just return that static piece of text. As a result, evaluating all
	 * returned expressions and concatenagint the results produces the complete evaluated
	 * string.
	 * @param expressionString the expression string
	 * @return the parsed expressions
	 * @throws ConversionException when the expressions cannot be parsed
	 */
	protected Expression[] parseExpressions(String expressionString) throws ConversionException {
		List expressions = new LinkedList();
		if (StringUtils.hasText(expressionString)) {
			int startIdx = 0;
			while (startIdx < expressionString.length()) {
				int exprStartIdx = expressionString.indexOf("${", startIdx);
				if (exprStartIdx >= startIdx) {
					// an expression was found
					if (exprStartIdx > startIdx) {
						expressions.add(new StaticExpression(expressionString.substring(startIdx, exprStartIdx)));
						startIdx = exprStartIdx;
					}
					
					int exprEndIdx = expressionString.indexOf("}", exprStartIdx);
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

	/**
	 * Parse given expression string, which should be contained in a "${...}" marker.
	 * @param expressionString the expression string 
	 * @return the parsed expression
	 * @throws ConversionException when the expression cannot be parsed
	 */
	protected Expression parseExpression(String expressionString) throws ConversionException {
		try {
//			Assert.state(getExpressionParser().isExpression(expressionString), "the input string '" + expressionString + "' is not an expression");
			return getExpressionParser().parseExpression(expressionString);
		}
		catch (ParserException e) {
			throw new ConversionException(expressionString, Expression.class, e);
		}
	}

	/**
	 * Helper that parses given encoded class (which should start with "class:") and
	 * instantiates the identified class using the default constructor.
	 * @param encodedClass the encoded class reference, starting with "class:"
	 * @return an instantiated objected of the identified class
	 * @throws ConversionException when the class cannot be found or cannot be instantiated
	 */
	protected Object parseAndInstantiateClass(String encodedClass) throws ConversionException {
		try {
			Assert.state(encodedClass.startsWith(CLASS_PREFIX));
			String className = encodedClass.substring(CLASS_PREFIX.length());
			Class clazz = (Class)new TextToClass().convert(className);
			return BeanUtils.instantiateClass(clazz);
		}
		catch (BeansException e) {
			throw new ConversionException(encodedClass, Object.class, e);
		}
	}
}
