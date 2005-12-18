package org.springframework.webflow.execution;

import java.util.Map;

import org.springframework.webflow.ExternalContext;

/**
 * A data source accessor that accesses the external context session map.
 * @author Keith Donald
 */
public class SessionDataStoreAccessor implements DataStoreAccessor {
	public Map getDataStore(ExternalContext context) {
		return context.getSessionMap();
	}
}