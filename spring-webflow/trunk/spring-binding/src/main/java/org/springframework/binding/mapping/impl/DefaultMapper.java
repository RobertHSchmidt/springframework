/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.binding.mapping.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.mapping.Mapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingResults;
import org.springframework.core.style.ToStringCreator;

/**
 * Generic mapper implementation that allows mappings to be configured programatically.
 * @author Keith Donald
 */
public class DefaultMapper implements Mapper {

	private static final Log logger = LogFactory.getLog(DefaultMapper.class);

	private List mappings = new LinkedList();

	private ConversionService conversionService;

	/**
	 * Returns the conversion service to use to perform type conversions as needed by mapping operations. May be null.
	 */
	public ConversionService getConversionService() {
		return conversionService;
	}

	/**
	 * Sets the conversion service to use to perform type conversions as needed by mapping operations. Optional.
	 * @param conversionService the conversion service;
	 */
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Add a mapping to this mapper.
	 * @param mapping the mapping to add
	 * @return this, to support convenient call chaining
	 */
	public DefaultMapper addMapping(DefaultMapping mapping) {
		mappings.add(mapping);
		return this;
	}

	/**
	 * Returns this mapper's list of mappings.
	 * @return the list of mappings
	 */
	public Mapping[] getMappings() {
		return (Mapping[]) mappings.toArray(new Mapping[mappings.size()]);
	}

	public MappingResults map(Object source, Object target) {
		DefaultMappingContext context = new DefaultMappingContext(source, target, conversionService);
		Iterator it = mappings.iterator();
		while (it.hasNext()) {
			DefaultMapping mapping = (DefaultMapping) it.next();
			mapping.map(context);
		}
		MappingResults results = context.toResult();
		if (logger.isDebugEnabled()) {
			logger.debug("Mapping operation completed; results = " + results);
		}
		return results;
	}

	public String toString() {
		return new ToStringCreator(this).append("mappings", mappings).toString();
	}
}