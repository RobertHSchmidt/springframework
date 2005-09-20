package org.springframework.webflow.execution;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.webflow.Event;

public interface ExternalScopeAccessor {
	public MutableAttributeSource getScope(Event sourceEvent, boolean createScope);
}
