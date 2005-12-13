package org.springframework.webflow.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple parameter object that holds information used to construct a flow
 * definition: most notably the flow identifier to assign and any flow
 * properties to assign.
 * 
 * @author Keith Donald
 */
public class FlowParameters {
	
	/**
	 * The flow id.
	 */
	private String id;

	/**
	 * The flow properties. 
	 */
	private Map properties = new HashMap(6);

	/**
	 * Default constructor for bean-style usage.
	 * @see #setId(String)
	 * @see #setProperties(Map)
	 */
	public FlowParameters() {

	}

	/**
	 * Creates a flow parameters value object containing the specified flow id
	 * and an empty properties map.
	 * @param id the flow Id
	 */
	public FlowParameters(String id) {
		setId(id);
	}

	/**
	 * Returns the flow id parameter.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the flow id parameter.
	 * @param id the flow id parameter.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the flow properties map.
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * Sets the flow properties map.
	 * @param properties the flow properties map
	 */
	public void setProperties(Map properties) {
		this.properties = properties;
	}
}