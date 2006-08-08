/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.core.collection.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;

import org.springframework.binding.util.MapAccessor;
import org.springframework.core.style.StylerUtils;
import org.springframework.webflow.core.collection.AttributeMap;

/**
 * A base class for map decorators who manage the storage of String-keyed
 * attributes in a backing {@link Map} implementation. This base provides
 * convenient operations for accessing attributes in a typed-manner.
 * 
 * @author Keith Donald
 */
public abstract class AbstractAttributeMap implements AttributeMap {

	/**
	 * The backing map storing the attributes.
	 */
	private Map attributes;

	/**
	 * A helper for accessing attributes. Marked transient and restored on
	 * deserialization.
	 */
	private transient MapAccessor attributeAccessor;

	protected AbstractAttributeMap() {
		
	}
	
	public AbstractAttributeMap(Map targetMap) {
		initAttributes(targetMap);
	}
	
	public Map getMap() {
		return attributeAccessor.getMap();
	}

	public int size() {
		return attributes.size();
	}

	public Object get(String attributeName) {
		return attributes.get(attributeName);
	}

	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	public boolean contains(String attributeName) {
		return attributes.containsKey(attributeName);
	}

	public boolean contains(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.containsKey(attributeName, requiredType);
	}

	public Object get(String attributeName, Object defaultValue) {
		return attributeAccessor.get(attributeName, defaultValue);
	}

	public Object get(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.get(attributeName, requiredType);
	}

	public Object get(String attributeName, Class requiredType, Object defaultValue) throws IllegalStateException {
		return attributeAccessor.get(attributeName, requiredType, defaultValue);
	}

	public Object getRequired(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequired(attributeName);
	}

	public Object getRequired(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequired(attributeName, requiredType);
	}

	public String getString(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getString(attributeName);
	}

	public String getString(String attributeName, String defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getString(attributeName, defaultValue);
	}

	public String getRequiredString(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredString(attributeName);
	}

	public Collection getCollection(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getCollection(attributeName);
	}

	public Collection getCollection(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getCollection(attributeName, requiredType);
	}

	public Collection getRequiredCollection(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredCollection(attributeName);
	}

	public Collection getRequiredCollection(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredCollection(attributeName, requiredType);
	}

	public Object[] getArray(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getArray(attributeName, requiredType);
	}

	public Object[] getRequiredArray(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredArray(attributeName, requiredType);
	}

	public Number getNumber(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getNumber(attributeName, requiredType);
	}

	public Number getNumber(String attributeName, Class requiredType, Number defaultValue)
			throws IllegalArgumentException {
		return attributeAccessor.getNumber(attributeName, requiredType, defaultValue);
	}

	public Number getRequiredNumber(String attributeName, Class requiredType) throws IllegalArgumentException {
		return attributeAccessor.getRequiredNumber(attributeName, requiredType);
	}

	public Integer getInteger(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getInteger(attributeName);
	}

	public Integer getInteger(String attributeName, Integer defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getInteger(attributeName, defaultValue);
	}

	public Integer getRequiredInteger(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredInteger(attributeName);
	}

	public Long getLong(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getLong(attributeName);
	}

	public Long getLong(String attributeName, Long defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getLong(attributeName, defaultValue);
	}

	public Long getRequiredLong(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredLong(attributeName);
	}

	public Boolean getBoolean(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getBoolean(attributeName);
	}

	public Boolean getBoolean(String attributeName, Boolean defaultValue) throws IllegalArgumentException {
		return attributeAccessor.getBoolean(attributeName, defaultValue);
	}

	public Boolean getRequiredBoolean(String attributeName) throws IllegalArgumentException {
		return attributeAccessor.getRequiredBoolean(attributeName);
	}

	// helpers for subclasses

	/**
	 * Initializes this attribute map.
	 * @param attributes the attributes
	 */
	protected void initAttributes(Map attributes) {
		this.attributes = attributes;
		attributeAccessor = new MapAccessor(this.attributes);
	}

	/**
	 * Returns the wrapped, modifiable map implementation.
	 */
	protected Map getMapInternal() {
		return attributes;
	}

	// custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		attributeAccessor = new MapAccessor(attributes);
	}

	public String toString() {
		return StylerUtils.style(attributes);
	}
}