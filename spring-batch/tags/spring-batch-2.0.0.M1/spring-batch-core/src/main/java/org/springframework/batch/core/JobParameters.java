/**
 * 
 */
package org.springframework.batch.core;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * This class is immutable and therefore thread-safe.
 * 
 * @author Lucas Ward
 * @since 1.0
 */
public class JobParameters implements Serializable {

	private final Map<String,JobParameter> parameters;
	
	public JobParameters() {
		this.parameters = new LinkedHashMap<String, JobParameter>();
	}
	
	public JobParameters(Map<String,JobParameter> parameters) {
		this.parameters = new LinkedHashMap<String,JobParameter>(parameters);
	}
	
	/**
	 * Typesafe Getter for the Long represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>Long</code> value
	 */
	public long getLong(String key){
		return ((Long)parameters.get(key).getValue()).longValue();
	}
	
	/**
	 * Typesafe Getter for the Long represented by the provided key.  If the
	 * key does not exist, the default value will be returned.
	 * 
	 * @param key to return the value for
	 * @param defaultValue to return if the value doesn't exist
	 * @return the parameter represented by the provided key, defaultValue 
	 * otherwise.
	 */
	public long getLong(String key, long defaultValue){
		if(parameters.containsKey(key)){
			return getLong(key);
		}
		else{
			return defaultValue;
		}
	}

	/**
	 * Typesafe Getter for the String represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>String</code> value
	 */
	public String getString(String key){
		return parameters.get(key).toString();
	}
	
	/**
	 * Typesafe Getter for the String represented by the provided key.  If the
	 * key does not exist, the default value will be returned.
	 * 
	 * @param key to return the value for
	 * @param defaultValue to return if the value doesn't exist
	 * @return the parameter represented by the provided key, defaultValue 
	 * otherwise.
	 */
	public String getString(String key, String defaultValue){
		if(parameters.containsKey(key)){
			return getString(key);
		}
		else{
			return defaultValue;
		}
	}
	
	/**
	 * Typesafe Getter for the Long represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>Double</code> value
	 */
	public double getDouble(String key){
		return ((Double)parameters.get(key).getValue()).doubleValue();
	}
	
	/**
	 * Typesafe Getter for the Double represented by the provided key.  If the
	 * key does not exist, the default value will be returned.
	 * 
	 * @param key to return the value for
	 * @param defaultValue to return if the value doesn't exist
	 * @return the parameter represented by the provided key, defaultValue 
	 * otherwise.
	 */
	public double getDouble(String key, double defaultValue){
		if(parameters.containsKey(key)){
			return getDouble(key);
		}
		else{
			return defaultValue;
		}
	}
	
	/**
	 * Typesafe Getter for the Date represented by the provided key.
	 * 
	 * @param key The key to get a value for
	 * @return The <code>java.util.Date</code> value
	 */
	public Date getDate(String key){
		return (Date)parameters.get(key).getValue();
	}
	
	/**
	 * Typesafe Getter for the Date represented by the provided key.  If the
	 * key does not exist, the default value will be returned.
	 * 
	 * @param key to return the value for
	 * @param defaultValue to return if the value doesn't exist
	 * @return the parameter represented by the provided key, defaultValue 
	 * otherwise.
	 */
	public Date getDate(String key, Date defaultValue){
		if(parameters.containsKey(key)){
			return getDate(key);
		}
		else{
			return defaultValue;
		}
	}
	
	/**
	 * Get a map of all parameters, including string, long, and date. It should
	 * be noted that a Collections$UnmodifiableMap is returned, ensuring
	 * immutability.
	 * 
	 * @return an unmodifiable map containing all parameters.
	 */
	public Map<String, JobParameter> getParameters(){
		return new LinkedHashMap<String, JobParameter>(parameters);
	}
	
	/**
	 * @return true if the prameters is empty, false otherwise.
	 */
	public boolean isEmpty(){
		return parameters.isEmpty();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JobParameters == false){
			return false;
		}
		
		if(obj == this){
			return true;
		}
		
		JobParameters rhs = (JobParameters)obj;
		return this.parameters.equals(rhs.parameters); 
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(7, 21).append(parameters).toHashCode();
	}
	
	@Override
	public String toString() {
		return parameters.toString();
	}
}
