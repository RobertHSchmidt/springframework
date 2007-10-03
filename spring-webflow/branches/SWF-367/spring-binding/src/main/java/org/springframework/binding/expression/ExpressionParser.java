/*
 * Copyright 2004-2007 the original author or authors.
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

/**
 * Parses expression strings, returning a configured evaluator instance capable of performing parsed expression
 * evaluation in a thread safe way.
 * 
 * @author Keith Donald
 */
public interface ExpressionParser {

	/**
	 * Parse the provided expression string, returning an expression evalutor capable of evaluating it.
	 * @param expressionString the parseable expression string
	 * @param expressionTargetType the class of target object this expression can successfully evaluate
	 * @param expressionVariables variables providing aliases for this expression during evaluation
	 * @param isAlwaysAnEvalExpression a flag indicating whether the expression being parsed is always an "eval"
	 * expression or not; an eval expression is always dynamic and is never a literal expression. If true, the parser
	 * may transform the provided expressionString by enclosing it in explicit "eval delimiters" like #{} before
	 * parsing. Usually false, and some parsers may not care about this flag.
	 * @return the evaluator for the parsed expression
	 * @throws ParserException an exception occurred during parsing
	 */
	public Expression parseExpression(String expressionString, Class expressionTargetType,
			ExpressionVariable[] expressionVariables, boolean isAlwaysAnEvalExpression) throws ParserException;
}