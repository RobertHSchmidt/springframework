package org.springframework.webflow.config;

import java.util.List;

import org.springframework.core.style.StylerUtils;
import org.springframework.webflow.access.ArtifactLookupException;
import org.springframework.webflow.access.NoSuchArtifactException;

/**
 * A lookup exception thrown when a composite flow artifact locator cannot
 * locate an artifact.
 * @author Keith Donald
 */
public class FlowArtifactLocatorChainExaustedException extends NoSuchArtifactException {

	/**
	 * The individual lookup exceptions thrown during the composite lookup
	 * operation.
	 */
	private ArtifactLookupException[] lookupExceptions;

	/**
	 * Constructs an exception indicating an artifact locator chain was
	 * exhausted with no match for the request artifact.
	 * @param artifactType the artifact type
	 * @param id the artifact id
	 * @param lookupExceptions the individual lookup exceptions
	 */
	public FlowArtifactLocatorChainExaustedException(Class artifactType, String id, List lookupExceptions) {
		super(artifactType, id, "Flow artifact locator chain exhausted looking for artifact of type: " + artifactType
				+ " with id: '" + id + "', " + lookupExceptions.size() + " lookup exceptions thrown, they are: "
				+ StylerUtils.style(lookupExceptions), null);
		this.lookupExceptions = (ArtifactLookupException[])lookupExceptions
				.toArray(new ArtifactLookupException[0]);
	}

	/**
	 * Returns the lookup exceptions thrown during the composite flow artifact
	 * lookup operation.
	 */
	public ArtifactLookupException[] getLookupExceptions() {
		return lookupExceptions;
	}
}