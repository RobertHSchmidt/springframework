/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.binding.format;

/**
 * A lightweight interface for formatting a value and parsing a value from its
 * formatted form.
 * @author Keith Donald
 */
public interface Formatter {

	/**
	 * Format the value.
	 * @param value the value to format
	 * @return The formatted string, fit for display in a UI
	 * @throws IllegalArgumentException The value could not be formatted.
	 */
	public String formatValue(Object value) throws IllegalArgumentException;

	/**
	 * Parse the formatted string representation of a value, restoring the
	 * value.
	 * @param formattedString The formatted string representation
	 * @param targetClass the target class to convert the formatted value to
	 * @return The parsed value
	 * @throws InvalidFormatException The string was in an invalid form
	 */
	public Object parseValue(String formattedString, Class targetClass) throws InvalidFormatException;

}