package org.springframework.webflow.execution;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.ExternalContext;

/**
 * A data source accessor that accesses the external context session map.
 * @author Keith Donald
 */
public class SessionDataStoreAccessor implements DataStoreAccessor {
	public MutableAttributeSource getDataStore(ExternalContext context) {
		return new MapAttributeSource(context.getSessionMap());
	}
}