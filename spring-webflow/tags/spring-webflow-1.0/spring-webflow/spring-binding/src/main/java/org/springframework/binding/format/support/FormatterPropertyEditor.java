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
package org.springframework.binding.format.support;

import java.beans.PropertyEditorSupport;

import org.springframework.binding.format.Formatter;

/**
 * Adapts a formatter to the property editor interface.
 * @author Keith Donald
 */
public class FormatterPropertyEditor extends PropertyEditorSupport {

	/**
	 * The formatter
	 */
	private Formatter formatter;

	/**
	 * The target value class (may be null).
	 */
	private Class targetClass;

	/**
	 * Creates a formatter property editor.
	 * @param formatter the formatter to adapt
	 */
	public FormatterPropertyEditor(Formatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * Creates a formatter property editor.
	 * 
	 * @param formatter the formatter to adapt
	 * @param targetClass the target class for "setAsText" conversions.
	 */
	public FormatterPropertyEditor(Formatter formatter, Class targetClass) {
		this.formatter = formatter;
		this.targetClass = targetClass;
	}

	public String getAsText() {
		return formatter.formatValue(getValue());
	}

	public void setAsText(String text) throws IllegalArgumentException {
		setValue(formatter.parseValue(text, targetClass));
	}
}