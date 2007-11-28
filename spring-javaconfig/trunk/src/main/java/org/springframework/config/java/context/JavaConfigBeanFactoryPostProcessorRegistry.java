/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java.context;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.config.java.process.ConfigurationPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * TODO: Document
 * 
 * @author Chris Beams
 */
public class JavaConfigBeanFactoryPostProcessorRegistry {

	public Collection<BeanFactoryPostProcessor> postProcessors = new ArrayList<BeanFactoryPostProcessor>();

	/**
	 * register all default BFPPs
	 */
	{
		postProcessors.add(new ConfigurationPostProcessor());
	}

	/**
	 * Adds all registered {@link BeanFactoryPostProcessor}s to <var>context</var>
	 * 
	 * @param context
	 */
	public void addAllPostProcessors(ConfigurableApplicationContext context) {
		for (BeanFactoryPostProcessor postProcessor : postProcessors)
			context.addBeanFactoryPostProcessor(postProcessor);
	}
}
