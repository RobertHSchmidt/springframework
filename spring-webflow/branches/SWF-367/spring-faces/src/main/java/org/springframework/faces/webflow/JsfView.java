package org.springframework.faces.webflow;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.View;

public class JsfView extends View {

	public static final String EVENT_KEY = "org.springframework.webflow.FacesEvent";

	public static final String STATE_KEY = "org.springframework.webflow.FacesState";

	/**
	 * The root of the JSF component tree managed by this view
	 */
	private UIViewRoot viewRoot;

	private boolean eventSignaled = false;

	private Event event;

	public JsfView(UIViewRoot viewRoot) {
		this.viewRoot = viewRoot;

		String jsfEvent = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.get(EVENT_KEY);
		if (StringUtils.hasText(jsfEvent)) {
			this.eventSignaled = true;
			this.event = new Event(this, jsfEvent);
		}
	}

	public boolean eventSignaled() {
		return eventSignaled;
	}

	public Event getEvent() {
		return event;
	}

	public UIViewRoot getViewRoot() {
		return this.viewRoot;
	}

	public void render() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.renderResponse();
		try {
			facesContext.getApplication().getViewHandler().renderView(facesContext, viewRoot);
		} catch (IOException e) {
			throw new FacesException("An I/O error occurred during view rendering", e);
		} finally {
			facesContext.responseComplete();
		}
	}

}
