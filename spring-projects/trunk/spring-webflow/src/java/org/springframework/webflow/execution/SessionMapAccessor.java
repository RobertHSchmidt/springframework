package org.springframework.webflow.execution;

import java.util.Map;

import org.springframework.webflow.ExternalContext;

/**
 * A map accessor that accesses the external context session map.
 * @author Keith Donald
 */
public class SessionMapAccessor implements MapAccessor {
	public Map getMap(ExternalContext context) {
		return context.getSessionMap();
	}
}