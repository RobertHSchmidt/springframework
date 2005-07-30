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
package org.springframework.webflow.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;

/**
 * Conversion service used by the web flow system. This service
 * supports conversion for a number of web flow specific types.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowConversionService extends DefaultConversionService {

	private static final String DEFAULT_CONVERTERS_FILE = "FlowConversionService.properties";
	
	private static final Properties DEFAULT_CONVERTERS = new Properties();

	static {
		// Load default web flow specific converters from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_CONVERTERS_FILE, FlowConversionService.class);
			InputStream is = resource.getInputStream();
			try {
				DEFAULT_CONVERTERS.load(is);
			}
			finally {
				is.close();
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load '" + DEFAULT_CONVERTERS_FILE + "': " + ex.getMessage());
		}
	}
	
	/**
	 * Create a new web flow conversion service. The default web flow
	 * specific converters will automatically be registered.
	 */
	public FlowConversionService() {
		registerDefaultConverters();
	}
	
	/**
	 * Register the default web flow specific converters with this conversion service.
	 * @throws ConversionException when something goes horribly wrong
	 */
	protected void registerDefaultConverters() throws ConversionException {
		for (Enumeration props = DEFAULT_CONVERTERS.propertyNames(); props.hasMoreElements(); ) {
			String targetClassName = (String)props.nextElement();
			Class targetClass = (Class)getConversionExecutor(String.class, Class.class).execute(targetClassName);
			String converterClassName = (String)DEFAULT_CONVERTERS.getProperty(targetClassName);
			Class converterClass = (Class)getConversionExecutor(String.class, Class.class).execute(converterClassName);
			try {
				addConverter((Converter)converterClass.newInstance());
			}
			catch (IllegalAccessException e) {
				// should not happen
				throw new ConversionException(converterClassName, Converter.class, e,
						"Cannot access constructor for converter of type '" + converterClass + "'");
			}
			catch (InstantiationException e) {
				// should not happen
				throw new ConversionException(converterClassName, Converter.class, e,
						"Cannot instantiate converter of type '" + converterClass + "'");
			}
			addDefaultAlias(targetClass);
		}
	}
}