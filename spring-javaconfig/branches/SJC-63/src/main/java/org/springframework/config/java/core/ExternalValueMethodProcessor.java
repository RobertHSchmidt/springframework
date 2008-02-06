/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.config.java.core;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.config.java.annotation.ExternalValue;
import org.springframework.config.java.valuesource.ValueResolutionException;
import org.springframework.config.java.valuesource.ValueSource;
import org.springframework.util.Assert;

public class ExternalValueMethodProcessor extends AbstractBeanMethodProcessor {

	private final ValueSource valueSource;

	public ExternalValueMethodProcessor(ValueSource valueSource) {
		super(ExternalValue.class);
		this.valueSource = valueSource;
	}

	private ExternalValueMethodProcessor() {
		super(ExternalValue.class);
		this.valueSource = null;
	}

	public Object processMethod(Method m) throws ValueResolutionException {
		ExternalValue ev = m.getAnnotation(ExternalValue.class);
		Assert.notNull(ev, "method must be annotated with @ExternalValue");

		String name = ev.value();
		if ("".equals(name)) {
			name = m.getName();
			// Strip property name if needed
			if (name.startsWith("get")) {
				name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
			}
		}

		return valueSource.resolve(name, m.getReturnType());
	}

	public static boolean isExternalValueCreationMethod(Method candidateMethod) {
		return new ExternalValueMethodProcessor().understands(candidateMethod);
	}

	public static Collection<Method> findExternalValueCreationMethods(Class<?> configurationClass) {
		return new ExternalValueMethodProcessor().findMatchingMethods(configurationClass);
	}

}
