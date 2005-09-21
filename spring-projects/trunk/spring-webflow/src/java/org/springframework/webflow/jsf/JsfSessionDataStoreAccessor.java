package org.springframework.webflow.jsf;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.DataStoreAccessor;

public class JsfSessionDataStoreAccessor implements DataStoreAccessor {
	public MutableAttributeSource getDataStore(Event sourceEvent, boolean createDataStore) {
		JsfEvent event = (JsfEvent)sourceEvent;
		return new MapAttributeSource(event.getContext().getExternalContext().getSessionMap());
	}
}