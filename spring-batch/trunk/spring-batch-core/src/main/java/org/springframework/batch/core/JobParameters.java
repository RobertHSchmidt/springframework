/**
 * 
 */
package org.springframework.batch.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Value object representing runtime parameters to a batch job. Because the
 * parameters have no individual meaning outside of the JobParameters they are
 * contained within, it is a value object rather than an entity. It is also
 * extremely important that a parameters object can be reliably compared to
 * another for equality, in order to determine if one JobParameters object
 * equals another. Furthermore, because these parameters will need to be
 * persisted, it is vital that the types added are restricted.
 * 
 * @author Lucas Ward
 * @since 1.0
 */
public class JobParameters implements Serializable {

	private final Map stringMap;

	private final Map longMap;

	private final Map doubleMap;

	private final Map dateMap;

	/**
	 * Default constructor. Creates a new empty JobRuntimeParameters. It should
	 * be noted that this constructor should only be used if an empty parameters
	 * is needed, since JobRuntimeParameters is immutable.
	 */
	public JobParameters() {
		this.stringMap = new LinkedHashMap();
		this.longMap = new LinkedHashMap();
		this.doubleMap = new LinkedHashMap();
		this.dateMap = new LinkedHashMap();
	}

	/**
	 * Create a new parameters object based upon the maps for each of the
	 * supported data types. See {@link JobParametersBuilder} for an easier way
	 * to create parameters.
	 */
	public JobParameters(Map stringMap, Map longMap, Map doubleMap, Map dateMap) {
		super();

		validateMap(stringMap, String.class);
		validateMap(longMap, Long.class);
		validateMap(doubleMap, Double.class);
		validateMap(dateMap, Date.class);
		this.stringMap = new LinkedHashMap(stringMap);
		this.longMap = new LinkedHashMap(longMap);
		this.doubleMap = new LinkedHashMap(doubleMap);
		this.dateMap = copyDateMap(dateMap);
	}

	/**
	 * Typesafe Getter for the String represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>String</code> value
	 */
	public String getString(String key) {
		return (String) stringMap.get(key);
	}

	/**
	 * Typesafe Getter for the Long represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>Long</code> value
	 */
	public Long getLong(String key) {
		return (Long) longMap.get(key);
	}

	/**
	 * Typesafe Getter for the Long represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>Double</code> value
	 */
	public Double getDouble(String key) {
		return (Double) doubleMap.get(key);
	}

	/**
	 * Typesafe Getter for the Date represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>java.util.Date</code> value
	 */
	public Date getDate(String key) {
		return (Date) dateMap.get(key);
	}

	/**
	 * Get a map of all parameters, including string, long, and date. It should
	 * be noted that a Collections$UnmodifiableMap is returned, ensuring
	 * immutability.
	 * 
	 * @return an unmodifiable map containing all parameters.
	 */
	public Map getParameters() {
		Map tempMap = new LinkedHashMap(stringMap);
		tempMap.putAll(longMap);
		tempMap.putAll(doubleMap);
		tempMap.putAll(dateMap);
		return Collections.unmodifiableMap(tempMap);
	}

	/**
	 * Get a map of only string parameters.
	 * 
	 * @return String parameters.
	 */
	public Map getStringParameters() {
		return Collections.unmodifiableMap(stringMap);
	}

	/**
	 * Get a map of only Long parameters
	 * 
	 * @return long parameters.
	 */
	public Map getLongParameters() {
		return Collections.unmodifiableMap(longMap);
	}

	/**
	 * Get a map of only Double parameters
	 * 
	 * @return long parameters.
	 */
	public Map getDoubleParameters() {
		return Collections.unmodifiableMap(doubleMap);
	}

	/**
	 * Get a map of only Date parameters
	 * 
	 * @return date parameters.
	 */
	public Map getDateParameters() {
		return Collections.unmodifiableMap(dateMap);
	}

	/**
	 * @return true if the prameters is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return (dateMap.isEmpty() && longMap.isEmpty() && doubleMap.isEmpty() && stringMap.isEmpty());
	}

	/*
	 * Convenience method for validating that a the provided map only contains a
	 * particular type as a value, with only a String as a key.
	 */
	private void validateMap(Map map, Class type) {

		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {

			Entry entry = (Entry) it.next();
			if (entry.getKey() instanceof String == false) {
				throw new IllegalArgumentException("All parameter keys must be strings.");
			}
			if (entry.getValue().getClass() != type) {
				throw new IllegalArgumentException("The values in this map must be of type:[" + type + "].");
			}
		}
	}

	/*
	 * Convenience method for copying Date values to ensure immutability.
	 */
	private Map copyDateMap(Map dateMap) {
		Map tempMap = new LinkedHashMap();

		for (Iterator it = dateMap.entrySet().iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			Date date = (Date) entry.getValue();
			tempMap.put(entry.getKey(), new Date(date.getTime()));
		}

		return tempMap;
	}

	public boolean equals(Object obj) {

		if (obj instanceof JobParameters == false) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		JobParameters parameters = (JobParameters) obj;

		// Since the type contained by each map is known, it's safe to call
		// Map.equals()
		if (getParameters().equals(parameters.getParameters())) {
			return true;
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return new HashCodeBuilder(7, 21).append(stringMap).append(longMap).append(doubleMap).append(dateMap)
				.toHashCode();
	}

	public String toString() {

		return stringMap.toString() + longMap.toString() + doubleMap.toString() + dateMap.toString();
	}
}
