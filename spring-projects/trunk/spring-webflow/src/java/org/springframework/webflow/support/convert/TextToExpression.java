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

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.PropertyExpression;

/**
 * Converter that converts a String into an Expression object.
 * 
 * @see org.springframework.binding.expression.Expression
 * @see org.springframework.binding.expression.PropertyExpression
 * 
 * @author Erwin Vervaet
 */
public class TextToExpression extends BaseConverter {

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Expression.class, PropertyExpression.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		return parseExpression((String)source);
	}

}
