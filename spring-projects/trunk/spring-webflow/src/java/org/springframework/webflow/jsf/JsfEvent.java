package org.springframework.webflow.jsf;

import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.springframework.webflow.execution.ExternalEvent;

/**
 * External event that communicates a user action in a JSF environment;
 * either requesting that a new flow should be started or noting an event was
 * signaled in an ongoing flow execution.
 * 
 * This event provides access to the FacesContext associated with the
 * application. The JSF outcome is treated as the event identifier.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class JsfEvent extends ExternalEvent {

	/**
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 */
	private String actionId;

	/**
	 * Creates a JSF event reporting the specified outcome (as the eventId) in
	 * the specified context.
	 * @param outcome The logical outcome returned by the specified action
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 */
	public JsfEvent(String outcome, FacesContext context, String actionId) {
		super(context);
		setId(outcome);
		this.actionId = actionId;
		initParameters(context, null);
	}

	/**
	 * Creates a JSF event reporting the specified outcome (as the eventId) in
	 * the specified context.
	 * @param outcome The logical outcome returned by the specified action
	 * @param context <code>FacesContext</code> for the current request
	 * @param fromAction The action binding expression that was evaluated to
	 * retrieve the specified outcome (if any)
	 * @param parameters additional event parameters to add
	 */
	public JsfEvent(String outcome, FacesContext context, String actionId, Map parameters) {
		super(context);
		setId(outcome);
		this.actionId = actionId;
		initParameters(context, parameters);
	}

	/**
	 * Helper that initializes event parameters
	 * @param context the faces context
	 * @param parameters additional parameters to add
	 */
	protected void initParameters(FacesContext context, Map parameters) {
        setParameters(new TreeMap());
        // our event snapshots both request param and request attribute data
        // this is different than ServletEvent, but makes sense for JSF, where
        // JSF has potentially bound request-scope data as attributes
		addParameters(context.getExternalContext().getRequestParameterMap());
		addParameters(context.getExternalContext().getRequestMap());
		addParameters(parameters);
	}

	/**
	 * Returns the faces context.
	 */
	public FacesContext getFacesContext() {
		return (FacesContext)getSource();
	}

	/**
	 * Returns the action binding expression that was evaluated to retrieve the
	 * specified outcome (if any)
	 */
	public String getActionId() {
		return actionId;
	}

	public Map getApplicationMap() {
		return getFacesContext().getExternalContext().getApplicationMap();
	}

	public Map getSessionMap() {
		return getFacesContext().getExternalContext().getSessionMap();
	}
}