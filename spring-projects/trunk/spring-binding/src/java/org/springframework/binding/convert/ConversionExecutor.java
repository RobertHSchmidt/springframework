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
package org.springframework.binding.convert;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * A command object that is parameterized with the information neccessary to
 * perform a conversion of a source input to a target output.
 * <p>
 * Specifically, encapsulates knowledge about how to convert source objects to a
 * specific target type using a specific converter.
 * @author Keith Donald
 */
public class ConversionExecutor implements Serializable {

	/**
	 * The converter that will perform the conversion.
	 */
	private Converter converter;

	/**
	 * The target value type this executor will attempt to convert to.
	 */
	private Class targetClass;

	/**
	 * Creates a conversion executor.
	 * @param converter The converter that will perform the conversion.
	 * @param targetClass The target type that the converter will convert to.
	 */
	public ConversionExecutor(Converter converter, Class targetClass) {
		this.converter = converter;
		this.targetClass = targetClass;
	}

	/**
	 * Execute the conversion for the provided source object.
	 * @param source the source object to convert
	 */
	public Object execute(Object source) throws ConversionException {
		return converter.convert(source, targetClass, Collections.EMPTY_MAP);
	}

	/**
	 * Execute the conversion for the provided source object.
	 * @param source the source object to convert
	 * @param context the conversion context, useful for influencing
	 * the behavior of the converter.
	 */
	public Object execute(Object source, Map context) throws ConversionException {
		return converter.convert(source, targetClass, context);
	}
}