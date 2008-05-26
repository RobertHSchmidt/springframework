/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java.internal.parsing;

import java.util.Iterator;
import java.util.Stack;

import org.springframework.config.java.annotation.Import;
import org.springframework.core.io.Resource;

/**
 * Used by {@link ConfigurationParser} when detecting circular {@link Import @Import} usage
 *
 * <p>TODO: when porting to ASM, this class will need to work with a stack of {@link Resource}
 * objects rather than a stack of {@link Class} objects.
 *
 * @author Chris Beams
 */
@SuppressWarnings("serial")
public class CircularImportException extends IllegalStateException {
	public CircularImportException(Class<?> attemptedImport, Stack<Class<?>> currentImportStack) {
		super(String.format("A circular @Import has been detected: " +
				"Illegal attempt by @Configuration class '%s' to import class '%s' as '%s' is " +
				"already present in the current import stack [%s]",
				currentImportStack.peek().getSimpleName(),
				attemptedImport.getSimpleName(), attemptedImport.getSimpleName(),
				renderImportStack(currentImportStack)));
	}

	private static String renderImportStack(Stack<Class<?>> importStack) {
		StringBuilder b = new StringBuilder();
		Iterator<Class<?>> iterator = importStack.iterator();
		while(iterator.hasNext()) {
			b.append(iterator.next().getSimpleName());
			if(iterator.hasNext())
				b.append("->");
		}
		return b.toString();
	}

}