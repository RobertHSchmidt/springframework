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
package org.springframework.binding.method;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.util.StringUtils;

/**
 * Converter that takes an encoded string representation and produces a
 * corresponding <code>MethodKey</code> object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li> "methodName" - the name of the method to invoke, where the method is
 * expected to have no arguments. </li>
 * <li> "methodName(arg1Type arg1Name, argNType argNName)" - the name of the
 * method to invoke, where the method is expected to have arguments delimited by
 * a comma. In this example, the method has two arguments. The type is either
 * the fully-qualified class of the argument OR a known type alias. The name is
 * the logical name of the argument, which is used during data binding to
 * retrieve the argument value.
 * </ul>
 * 
 * @see org.springframework.webflow.action.bean.MethodKey
 * 
 * @author Keith Donald
 */
public class TextToMethodKey extends ConversionServiceAwareConverter {

	/**
	 * Create a new converter that converts strings to MethodKey objects.
	 */
	public TextToMethodKey() {
	}

	/**
	 * Create a new converter that converts strings to MethodKey objects.
	 */
	public TextToMethodKey(ConversionService conversionService) {
		super(conversionService);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { MethodKey.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedMethodKey = (String)source;
		encodedMethodKey = encodedMethodKey.trim();
		int openParam = encodedMethodKey.indexOf('(');
		if (openParam == -1) {
			return new MethodKey(encodedMethodKey);
		}
		else {
			String methodName = encodedMethodKey.substring(0, openParam);
			int closeParam = encodedMethodKey.lastIndexOf(')');
			if (closeParam == -1) {
				throw new ConversionException(encodedMethodKey, MethodKey.class, null,
						"Syntax error: No close parenthesis specified for argument list");
			}
			String argList = encodedMethodKey.substring(openParam + 1, closeParam);
			String[] args = StringUtils.commaDelimitedListToStringArray(argList);
			Arguments arguments = new Arguments(args.length);
			for (int i = 0; i < args.length; i++) {
				String arg = args[i].trim();
				String[] typeAndName = StringUtils.split(arg, " ");
				if (typeAndName.length == 2) {
					Class type = (Class)converterFor(String.class, Class.class).execute(typeAndName[0]);
					arguments.add(new Argument(type, typeAndName[1].trim()));
				} else {
					arguments.add(new Argument(typeAndName[0]));
				}
			}
			return new MethodKey(methodName, arguments);
		}
	}
}