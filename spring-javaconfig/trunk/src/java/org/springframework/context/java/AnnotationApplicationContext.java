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
package org.springframework.context.java;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Annotation-aware application context. This class extends
 * {@link AbstractAnnotationApplicationContext} and adds support for reading an
 * application context definition/metadata directly from a class.
 * 
 * @author Costin Leau
 * 
 */
public class AnnotationApplicationContext extends AbstractAnnotationApplicationContext {

	private String[] configLocations;

	private Resource[] configResources;

	private Class[] configClasses;

	/**
	 * Create a new AnnotationApplicationContext.
	 */
	public AnnotationApplicationContext() {
		super();
	}

	/**
	 * Create a new AnnotationApplicationContext with the given parent.
	 * 
	 * @param parent the parent application context
	 */
	public AnnotationApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Create a new AnnotationApplicationContext from the given locations.
	 * 
	 * @param locations the metadata locations
	 */
	public AnnotationApplicationContext(String... locations) {
		setConfigLocations(locations);
		refresh();
	}

	/**
	 * Create a new AnnotationApplicationContext from the given classes.
	 * 
	 * @param classes
	 */
	public AnnotationApplicationContext(Class... classes) {
		setConfigClasses(classes);
		refresh();
	}

	@Override
	protected String[] getConfigLocations() {
		return configLocations;
	}

	@Override
	protected Resource[] getConfigResources() {
		return configResources;
	}

	@Override
	protected Class[] getConfigClasses() {
		return configClasses;
	}

	/**
	 * Set the configuration locations from Strings. These will be converted
	 * into Spring's {@link Resource} using the current ${@link org.springframework.core.io.ResourceLoader}.
	 * 
	 * @param locations
	 */
	public void setConfigLocations(String... locations) {
		this.configLocations = locations;
	}

	/**
	 * Indicate the configuration locations as {@link Resource}s.
	 * 
	 * @param resources
	 */
	public void setConfigResources(Resource... resources) {
		this.configResources = resources;
	}

	/**
	 * Indicate the {@link Class}es that hold annotations suitable for
	 * configuring the current application context.
	 * @param classes
	 */
	public void setConfigClasses(Class... classes) {
		this.configClasses = classes;
	}
}
