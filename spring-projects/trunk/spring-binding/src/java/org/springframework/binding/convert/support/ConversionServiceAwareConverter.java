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
package org.springframework.binding.convert.support;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.ConversionServiceAware;
import org.springframework.util.Assert;


/**
 * Base class for converters that use other converters to convert things, thus
 * they are conversion-service aware.
 * 
 * @author Keith Donald
 */
public abstract class ConversionServiceAwareConverter extends AbstractConverter implements ConversionServiceAware {

	protected static final String CLASS_PREFIX = "class:";

	private ConversionService conversionService;

	public ConversionServiceAwareConverter() {
		
	}

	public ConversionServiceAwareConverter(ConversionService conversionService) {
		setConversionService(conversionService);
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	protected ConversionExecutor fromStringToTypeWithAlias(String targetAlias) {
		return conversionService.getConversionExecutorByTargetAlias(String.class, targetAlias);
	}
	
	protected ConversionExecutor fromStringTo(Class targetClass) {
		return conversionService.getConversionExecutor(String.class, targetClass);
	}
	
	protected ConversionExecutor converterFor(Class sourceClass, Class targetClass) {
		return conversionService.getConversionExecutor(sourceClass, targetClass);
	}

	/**
	 * Helper that parses given encoded class (which should start with "class:") and
	 * instantiates the identified class using the default constructor.
	 * @param encodedClass the encoded class reference, starting with "class:"
	 * @return an instantiated objected of the identified class
	 * @throws ConversionException when the class cannot be found or cannot be instantiated
	 */
	protected Object newInstance(String encodedClass) throws ConversionException {
		try {
			Assert.state(encodedClass.startsWith(CLASS_PREFIX), "The encoded class name should start with the class: prefix");
			String className = encodedClass.substring(CLASS_PREFIX.length());
			Class clazz = (Class)fromStringTo(Class.class).execute(className);
			return BeanUtils.instantiateClass(clazz);
		}
		catch (BeansException e) {
			throw new ConversionException(encodedClass, Object.class, e);
		}
	}
}