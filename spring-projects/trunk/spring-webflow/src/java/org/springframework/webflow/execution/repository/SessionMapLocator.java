package org.springframework.webflow.execution.repository;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ExternalContext.SharedMap;

/**
 * A {@link SharedMapLocator} returns the external context session map.
 * @author Keith Donald
 */
public class SessionMapLocator implements SharedMapLocator {
	public SharedMap getMap(ExternalContext context) {
		return context.getSessionMap();
	}
}