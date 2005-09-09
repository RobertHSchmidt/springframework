package org.springframework.webflow;

import java.util.Map;

import org.springframework.util.Assert;

/**
 * 
 * 
 * 
 * @author Steven Devijver
 */
public class ControllerInvocation {

	/**
	 * The source state that owns this controller invocation.
	 */
	ViewState sourceState = null;

	/**
	 * The eventId this controller responds to.
	 */
	String eventId = null;

	/**
	 * The controller to be invoked.
	 */
	Controller controller = null;

	/**
	 * The view descriptor creator that creates a view descriptor when this
	 * controller invocation is invoked.
	 */
	ViewDescriptorCreator viewDescriptorCreator = null;

	/**
	 * Default constructor for bean style usage.
	 *  
	 */
	public ControllerInvocation() {
		super();
	}

	/**
	 * Create a new controller invocation.
	 * 
	 * @param eventId
	 *                  the eventId to respond to
	 * @param controller
	 *                  the controller to be invoked
	 */
	public ControllerInvocation(String eventId, Controller controller) {
		this();
		Assert.notNull(eventId, "EventId is required!");
		setEventId(eventId);
		setController(controller);
	}

	/**
	 * Create a new controller invocation
	 * 
	 * @param eventId
	 *                  the eventId to respond to
	 * @param viewDescriptorCreator
	 *                  the view descriptor creator that creates a view descriptor
	 *                  when this controller invocation in invoked
	 * @param controller
	 *                  the controller to be invoked
	 */
	public ControllerInvocation(String eventId,
			ViewDescriptorCreator viewDescriptorCreator, Controller controller) {
		this(eventId, controller);
		setViewDescriptorCreator(viewDescriptorCreator);
	}

	/**
	 * Create a new controller invocation.
	 * 
	 * @param sourceState
	 *                  the state that owns this controller invocation
	 * @param viewDescriptorCreator
	 *                  the view descriptor creator that creates a view descriptor
	 *                  when this controller invocation in invoked
	 * @param eventId
	 *                  the eventId to respond to
	 * @param controller
	 *                  the controller to be invoked
	 */
	public ControllerInvocation(ViewState sourceState,
			ViewDescriptorCreator viewDescriptorCreator, String eventId,
			Controller controller) {
		this(eventId, controller);

	}

	/**
	 * Gets the eventId this controller invocation responds to.
	 * 
	 * @return the eventId
	 */
	public String getEventId() {
		return eventId;
	}

	/**
	 * Sets the eventId this controller invocation responds to.
	 * 
	 * @param eventId
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	/**
	 * Gets the source state that owns this controller invocation.
	 * 
	 * @return the source state
	 */
	public ViewState getSourceState() {
		return sourceState;
	}

	/**
	 * Sets the source state that owns this controller invocation.
	 * 
	 * @param sourceState
	 */
	public void setSourceState(ViewState sourceState) {
		this.sourceState = sourceState;
	}

	/**
	 * Gets the controller to be invoked by this controller invocation.
	 * 
	 * @return the controller
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * Sets the controller to be invoked by this controller invocation.
	 * 
	 * @param controller
	 *                  the controller
	 */
	public void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * Gets the view descriptor creator that creates a view descriptor when this
	 * controller invocation is invoked.
	 * 
	 * @return the view descriptor creator
	 */
	public ViewDescriptorCreator getViewDescriptorCreator() {
		return viewDescriptorCreator;
	}

	/**
	 * Sets the view descriptor creator that creates a view descriptor when this
	 * controller invocation is invoked.
	 * 
	 * @param viewDescriptorCreator
	 *                  the view descriptor creator
	 */
	public void setViewDescriptorCreator(
			ViewDescriptorCreator viewDescriptorCreator) {
		this.viewDescriptorCreator = viewDescriptorCreator;
	}

	/**
	 * Invokes the controller associated with this controller invocation.
	 * 
	 * @param flowScope
	 *                  the current flow scope
	 * @param event
	 *                  the event that triggered this request
	 * @return the view descriptor associated with this controller invocation
	 */
	public ViewDescriptor invoke(RequestContext context, Event event) {
		Map requestModel = null; 
		if (getController() != null) {
			requestModel = getController()
				.handle(context.getFlowScope(), event);
		}
		if (requestModel != null) {
			context.getRequestScope().putAll(requestModel);
		}
		if (getViewDescriptorCreator() != null) {
			return getViewDescriptorCreator().createViewDescriptor(context);
		} else {
			return null;
		}
	}

	/**
	 * Tests if this controller needs to be invoked for this eventId
	 * 
	 * @param eventId
	 *                  the eventId that's triggered
	 * @return controller needs to be invoked
	 */
	public boolean matches(String eventId) {
		return this.eventId.equals(eventId);
	}
}
