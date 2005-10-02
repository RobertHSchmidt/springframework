package org.springframework.webflow.jsf;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.DataStoreAccessor;

/**
 * Provides data store access to a JSF external context session map.
 * 
 * @author Keith Donald
 */
public class JsfSessionDataStoreAccessor implements DataStoreAccessor {
	public MutableAttributeSource getDataStore(Event sourceEvent, boolean createDataStore) {
		JsfEvent event = (JsfEvent)sourceEvent;
		return new MapAttributeSource(event.getFacesContext().getExternalContext().getSessionMap());
	}
}