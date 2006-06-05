package org.springframework.webflow.execution.repository.support;

import org.springframework.util.Assert;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.SharedMap;

/**
 * A {@link SharedMapLocator} that returns the external context session map.
 * @author Keith Donald
 */
public class SessionMapLocator implements SharedMapLocator {
	public SharedMap getMap(ExternalContext context) {
		Assert.notNull(context, "The external context is required");
		return context.getSessionMap().getSharedMap();
	}
	
	public boolean requiresRebindOnChange() {
		return true;
	}
}