package org.springframework.webflow.support;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

public final class MarkerViewSelector implements ViewSelector {

	public static final ViewSelector INSTANCE = new MarkerViewSelector();

	private MarkerViewSelector() {

	}

	public ViewSelection makeSelection(RequestContext context) {
		return ViewSelection.NULL_VIEW_SELECTION;
	}
}
