/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;

/**
 * A utility class for working with collections used by Spring Web FLow.
 * @author Keith Donald
 */
public class CollectionUtils {

	/**
	 * The shared, singleton empty unmodifiable map instance.
	 */
	public static final UnmodifiableAttributeMap EMPTY_ATTRIBUTE_MAP = new UnmodifiableAttributeMap(
			Collections.EMPTY_MAP);

	/**
	 * The shared, singleton empty unmodifiable parameter map instance.
	 */
	public static final ParameterMap EMPTY_PARAMETER_MAP = new ParameterMap(Collections.EMPTY_MAP);

	/**
	 * The shared, singleton empty enumeration instance.
	 */
	public static final EmptyEnumeration EMPTY_ENUMERATION = new EmptyEnumeration();

	private CollectionUtils() {

	}

	private static class EmptyEnumeration implements Enumeration, Serializable {
		private EmptyEnumeration() {

		}

		public boolean hasMoreElements() {
			return false;
		}

		public Object nextElement() {
			throw new UnsupportedOperationException("There are no elements");
		}
	}
}