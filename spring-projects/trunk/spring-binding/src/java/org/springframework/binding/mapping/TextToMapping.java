/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.binding.mapping;

import java.util.Map;

import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.util.StringUtils;

/**
 * Converts a text-encoded representation of a <code>Mapping</code> object to
 * a valid instance.
 * @author Keith Donald
 */
public class TextToMapping extends ConversionServiceAwareConverter {

	/**
	 * Creates a text to mapping converter.
	 * @param conversionService the conversion service.
	 */
	public TextToMapping(ConversionService conversionService) {
		super(conversionService);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Mapping.class };
	}

	protected Object doConvert(Object source, Class targetClass, Map context) throws Exception {
		// format:
		// <sourceExpression>[,class][->targetPropertyExpression[,class]]
		String[] sourceTarget = StringUtils.delimitedListToStringArray((String)source, "->");
		if (sourceTarget.length == 1) {
			// just target mapping info is specified
			String[] targetMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[0]);
			String sourceExpression = targetMappingInfo[0];
			String targetPropertyExpression = targetMappingInfo[0];
			Class targetValueType = null;
			if (targetMappingInfo.length == 2) {
				targetValueType = (Class)fromStringTo(Class.class).execute(targetMappingInfo[1]);
			}
			if (targetValueType != null) {
				return new Mapping(sourceExpression, targetPropertyExpression, getConversionService()
						.getConversionExecutor(String.class, targetValueType));
			}
			else {
				return new Mapping(sourceExpression, targetPropertyExpression);
			}
		}
		else {
			// source and target mapping info is specified
			String[] sourceMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[0]);
			String sourceExpression = sourceMappingInfo[0];
			Class sourceValueType = String.class;
			if (sourceMappingInfo.length == 2) {
				sourceValueType = (Class)fromStringTo(Class.class).execute(sourceMappingInfo[1]);
			}
			String[] targetMappingInfo = StringUtils.commaDelimitedListToStringArray(sourceTarget[1]);
			String targetPropertyExpression = targetMappingInfo[0];
			Class targetValueType = String.class;
			if (targetMappingInfo.length == 2) {
				targetValueType = (Class)fromStringTo(Class.class).execute(targetMappingInfo[1]);
			}
			if (!sourceValueType.equals(targetValueType)) {
				return new Mapping(sourceExpression, targetPropertyExpression, getConversionService()
						.getConversionExecutor(sourceValueType, targetValueType));
			}
			else {
				return new Mapping(sourceExpression, targetPropertyExpression);
			}
		}
	}
}