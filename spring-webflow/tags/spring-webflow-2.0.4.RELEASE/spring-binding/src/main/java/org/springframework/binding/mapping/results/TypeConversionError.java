/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.binding.mapping.results;

import org.springframework.binding.convert.ConversionExecutionException;
import org.springframework.binding.expression.ValueCoercionException;
import org.springframework.binding.mapping.Result;
import org.springframework.core.style.ToStringCreator;

/**
 * Indicates a type conversion occurred during a mapping operation.
 * 
 * @author Keith Donald
 * @author Scott Andrews
 */
public class TypeConversionError extends Result {

	private Object originalValue;

	private Class targetType;

	private Exception exception;

	/**
	 * Creates a new type conversion error.
	 * @param exception the underlying type conversion exception
	 */
	public TypeConversionError(ConversionExecutionException exception) {
		this.exception = exception;
		this.originalValue = exception.getValue();
		this.targetType = exception.getTargetClass();
	}

	public TypeConversionError(ValueCoercionException exception) {
		this.exception = exception;
		this.originalValue = exception.getValue();
		this.targetType = exception.getTargetClass();
	}

	public Object getOriginalValue() {
		return originalValue;
	}

	public Object getMappedValue() {
		return null;
	}

	public boolean isError() {
		return true;
	}

	public String getErrorCode() {
		return "typeMismatch";
	}

	// impl

	/**
	 * Returns the target type of the conversion attempt.
	 */
	public Class getTargetClass() {
		return targetType;
	}

	/**
	 * Returns the backing type conversion exception that occurred.
	 */
	public Exception getException() {
		return exception;
	}

	public String toString() {
		return new ToStringCreator(this).append("originalValue", originalValue).append("targetType", targetType)
				.append("message", exception.getMessage()).toString();
	}
}
