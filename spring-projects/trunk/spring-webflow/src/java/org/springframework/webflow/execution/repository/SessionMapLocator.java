package org.springframework.webflow.execution.repository;

import java.util.Map;

import org.springframework.webflow.ExternalContext;

/**
 * A map accessor that accesses the external context session map.
 * @author Keith Donald
 */
public class SessionMapLocator implements ExternalMapLocator {
	public Map getMap(ExternalContext context) {
		return context.getSessionMap();
	}
}