package org.springframework.webflow.registry;

import org.springframework.core.io.Resource;
import org.springframework.webflow.builder.FlowArtifactParameters;

/**
 * A descriptor for a Flow to be assembled from a externalized resource.
 * Describes exactly one externalized flow definition resource.
 * @author Keith Donald
 */
public class ExternalizedFlowDefinition extends FlowArtifactParameters {

	/**
	 * The externalized flow resource location.
	 */
	private Resource location;

	/**
	 * Default constructor for bean-style usage.
	 * @see FlowArtifactParameters#FlowParameters()
	 * @see #setLocation(Resource)
	 */
	public ExternalizedFlowDefinition() {

	}

	/**
	 * Creates a new externalized flow definition.
	 * @param id the flow id to be assigned
	 * @param location the flow resource location.
	 */
	public ExternalizedFlowDefinition(String id, Resource location) {
		super(id);
		setLocation(location);
	}

	/**
	 * Returns the externalized flow resource location.
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * Sets the externalized flow resource location.
	 * @param location the location
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}
}