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
package org.springframework.config.java.annotation;

/**
 * Internal interface implemented by all 'three-way' enumerations. Exists solely
 * to represent the relationship between these types; has package-private
 * visibility as it should not be used externally.
 * 
 * @see Lazy
 * @see Primary
 * 
 * @author Chris Beams
 */
interface BooleanEnum {
	int value();

	boolean booleanValue();
}
