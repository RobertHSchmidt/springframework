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
package org.springframework.webflow;

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Superclass of all objects in the web flow system that support annotation
 * using arbitrary properties. Mainly used to ensure consistent configuration of
 * properties for all annotated objects.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public abstract class AnnotatedObject {

	/**
	 * The caption (property name ("caption"). A caption is also known as a
	 * "short description" and may be used in a GUI tooltip.
	 */
	public static final String CAPTION_PROPERTY = "caption";

	/**
	 * The long description property name ("description"). A description
	 * provides additional, free-form detail about this object and might be
	 * shown in a GUI text area.
	 */
	public static final String DESCRIPTION_PROPERTY = "description";

	/**
	 * Additional properties further describing this object. The properties set
	 * in this map may be arbitrary.
	 */
	private Map properties = new HashMap(6);

	/**
	 * Returns the value of given property, or <code>null</code> if not found.
	 */
	public Object getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Set the value of named property.
	 * @param name the name of the property
	 * @param value the value to set
	 * @return previous value associated with specified name
	 */
	public Object setProperty(String name, Object value) {
		return properties.put(name, value);
	}

	/**
	 * Returns whether or not this annotated object contains a property with
	 * specified name.
	 * @param name the name of the property
	 * @return true if the property is set, false otherwise
	 */
	public boolean containsProperty(String name) {
		return properties.containsKey(name);
	}

	/**
	 * Returns a string property value.
	 * @param name the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public String getStringProperty(String name, String defaultValue) {
		if (containsProperty(name)) {
			return (String)getProperty(name);
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Returns an integer property value.
	 * @param name the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public int getIntProperty(String name, int defaultValue) {
		if (containsProperty(name)) {
            Object property = getProperty(name);
            Assert.isInstanceOf(Integer.class, property);
            return ((Integer)property).intValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Returns a boolean property value.
	 * @param name the property name
	 * @param defaultValue the default value, if the property is not set
	 * @return the property value
	 */
	public boolean getBooleanProperty(String name, boolean defaultValue) {
		if (containsProperty(name)) {
			return ((Boolean)getProperty(name)).booleanValue();
		}
		else {
			return defaultValue;
		}
	}

	/**
	 * Returns the short description of the action (suitable for display in a
	 * tooltip).
	 */
	public String getCaption() {
		return (String)getProperty(CAPTION_PROPERTY);
	}

	/**
	 * Sets the short description (suitable for display in a tooltip).
	 * @param caption the caption
	 */
	public void setCaption(String caption) {
		setProperty(CAPTION_PROPERTY, caption);
	}

	/**
	 * Returns the long description of this action.
	 */
	public String getDescription() {
		return (String)getProperty(DESCRIPTION_PROPERTY);
	}

	/**
	 * Sets the long description.
	 * @param description the long description
	 */
	public void setDescription(String description) {
		setProperty(DESCRIPTION_PROPERTY, description);
	}
	
	/**
	 * Returns the additional properties describing this object in an
	 * unmodifiable map.
	 */
	public Map getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/**
	 * Adds (puts) additional properties describing this object.
	 */
	public void addProperties(Map properties) {
		if (properties != null) {
			this.properties.putAll(properties);
		}
	}
}