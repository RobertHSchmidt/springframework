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
package org.springframework.binding.util;

import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A simple decorator for getting attributes out of a map. May be instantiated
 * directly or used as a base class as a convenience.
 * 
 * @author Keith Donald
 */
public class MapAccessor {

	/**
	 * The target map.
	 */
	private Map map;

	/**
	 * Creates a new attribute map accessor.
	 * @param map the map
	 */
	public MapAccessor(Map map) {
		Assert.notNull(map, "The map to decorate is required");
		this.map = map;
	}

	/**
	 * Returns the wrapped target map.
	 * @return the map
	 */
	public Map getMap() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Returns a value in the map, returning the defaultValue if no value was
	 * found.
	 * @param key the key
	 * @param defaultValue the default
	 * @return the attribute value
	 */
	public Object get(Object key, Object defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return map.get(key);
	}

	/**
	 * Returns an attribute value in the map, asserting it is of the required
	 * type if present.
	 * @param key the attribute name
	 * @param requiredType the required type
	 * @return the attribute value
	 */
	public Object get(Object key, Class requiredType) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return map.get(key);
		}
		return assertValueType(key, requiredType);
	}

	/**
	 * Returns an attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param key the attribute name
	 * @return the attribute value
	 */
	public Object getRequired(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return map.get(key);
	}

	/**
	 * Returns an attribute value in the map, asserting it is present and of the
	 * required type.
	 * @param key the attribute name
	 * @param requiredType the required type
	 * @return the attribute value
	 */
	public Object getRequired(Object key, Class requiredType) throws IllegalArgumentException {
		assertContainsKey(key);
		return assertValueType(key, requiredType);
	}

	/**
	 * Returns a string attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param key the attribute name
	 * @param defaultValue the default
	 * @return the stringattribute value
	 */
	public String getString(Object key, String defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return (String)assertValueType(key, String.class);
	}

	/**
	 * Returns a string attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param key the attribute name
	 * @return the string attribute value
	 */
	public String getRequiredString(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return (String)assertValueType(key, String.class);
	}

	/**
	 * Returns an int attribute value in the map, returning the defaultValue if
	 * no value was found.
	 * @param key the attribute name
	 * @param defaultValue the default
	 * @return the int attribute value
	 */
	public int getInt(Object key, int defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return ((Integer)assertValueType(key, Integer.class)).intValue();
	}

	/**
	 * Returns an int attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param key the attribute name
	 * @return the int attribute value
	 */
	public int getRequiredInt(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return ((Integer)assertValueType(key, Integer.class)).intValue();
	}

	/**
	 * Returns a long attribute value in the map, returning the defaultValue if
	 * no value was found.
	 * @param key the attribute name
	 * @param defaultValue the default
	 * @return the int attribute value
	 */
	public long getLong(Object key, long defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return ((Long)assertValueType(key, Long.class)).longValue();
	}

	/**
	 * Returns a long attribute value in the map, throwing an exception if the
	 * attribute is not present and of the correct type.
	 * @param key the attribute name
	 * @return the int attribute value
	 */
	public long getRequiredLong(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return ((Long)assertValueType(key, Long.class)).longValue();
	}

	/**
	 * Returns a boolean attribute value in the map, returning the defaultValue
	 * if no value was found.
	 * @param key the attribute name
	 * @param defaultValue the default
	 * @return the boolean attribute value
	 */
	public boolean getBoolean(Object key, boolean defaultValue) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			return defaultValue;
		}
		return ((Boolean)assertValueType(key, Boolean.class)).booleanValue();
	}

	/**
	 * Returns a boolean attribute value in the map, throwing an exception if
	 * the attribute is not present and of the correct type.
	 * @param key the attribute name
	 * @return the boolean attribute value
	 */
	public boolean getRequiredBoolean(Object key) throws IllegalArgumentException {
		assertContainsKey(key);
		return ((Boolean)assertValueType(key, Boolean.class)).booleanValue();
	}

	/**
	 * Asserts that the attribute is present in the attribute map.
	 * @param key the attribute name
	 * @param attributes the attribute map
	 * @return true if present, false if not present.
	 */
	public void assertContainsKey(Object key) throws IllegalArgumentException {
		if (!map.containsKey(key)) {
			throw new IllegalArgumentException("Required attribute '" + key
					+ "' is not present in map; attributes present are [" + getMap() + "]");
		}
	}

	/**
	 * Indicates if the attribute is present in the attribute map and of the
	 * required type.
	 * @param key the attribute name
	 * @return true if present and of the required type, false if not present.
	 */
	public boolean containsKey(Object key, Class requiredType) throws IllegalArgumentException {
		if (map.containsKey(key)) {
			assertValueType(key, requiredType);
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Assert that the type of the given attribute value is of the required
	 * type.
	 * @param key the attribute name
	 * @param type the required type
	 * @return the attribute value
	 */
	public Object assertValueType(Object key, Class requiredType) {
		Object value = map.get(key);
		if (!requiredType.isInstance(value)) {
			throw new IllegalArgumentException("Map key '" + key + "' has value [" + value
					+ "] that is not of expected type [" + requiredType + "], instead it is of type ["
					+ value.getClass() + "]");
		}
		return value;
	}
}