package org.springframework.webflow.jsf;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.Event;
import org.springframework.webflow.execution.ExternalScopeAccessor;

public class JsfSessionScopeAccessor implements ExternalScopeAccessor {
	public MutableAttributeSource getScope(Event sourceEvent, boolean createScope) {
		JsfEvent event = (JsfEvent)sourceEvent;
		return new MapAttributeSource(event.getContext().getExternalContext().getSessionMap());
	}
}