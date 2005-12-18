/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * Value object that provides clients with information about a logical view to
 * render and the dynamic model data neccessary to render it. It is expected
 * that clients map this view selection to a physical view template for
 * rendering (e.g. a JSP).
 * <p>
 * View selections are returned as a result of entering a {@link ViewState} or
 * {@link EndState}, typically created by those states delegating to a
 * {@link ViewSelector} factory. When a state of either of those types is
 * entered and returns, the caller into the web flow system is handed a
 * fully-configured <code>ViewSelection</code> instance and is expected to
 * present a screen to the user that allows them to interact at that point
 * within the flow.
 * <p>
 * For readers familiar with Spring MVC, this class is very similiar to the
 * <code>ModelAndView</code> construct. This class is provided to prevent a
 * web flow dependency on Spring MVC.
 * 
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ViewSelection implements Serializable {

	/**
	 * The name of the view (or page) to render.
	 */
	private String viewName;

	/**
	 * A map of the data available to the view for rendering.
	 */
	private Map model;

	/**
	 * Indicates whether or not the view should be rendered after a redirect.
	 */
	private boolean redirect = false;

	/**
	 * Default constructor for bean style usage.
	 */
	public ViewSelection() {
	}

	/**
	 * Convenient constructor when there is no model data to expose. Can also be
	 * used in conjunction with <code>addObject()</code>.
	 * @param viewName name of the view to render
	 * @see #addObject(String, Object)
	 */
	public ViewSelection(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Creates a new view selection given a view name and a model.
	 * @param viewName name of the view to render
	 * @param model map of model names (Strings) to model objects (Objects),
	 * model entries may not be null, but the model Map may be null if there is
	 * no model data
	 */
	public ViewSelection(String viewName, Map model) {
		this.viewName = viewName;
		this.model = model;
	}

	/**
	 * Convenient constructor to take a single model object.
	 * @param viewName name of the view to render
	 * @param modelName name of the single entry in the model
	 * @param modelObject the single model object
	 */
	public ViewSelection(String viewName, String modelName, Object modelObject) {
		this.viewName = viewName;
		addObject(modelName, modelObject);
	}

	/**
	 * Set a view name for this ViewSelection. Will override any pre-existing
	 * view name.
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * Return the view name.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Returns whether or not a redirect is necessary when rendering this view.
	 */
	public boolean isRedirect() {
		return this.redirect;
	}

	/**
	 * Set whether or not a redirect is necessary when rendering this view.
	 */
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

	/**
	 * Return the model map. Never returns null. To be called by application
	 * code for modifying the model.
	 */
	public Map getModel() {
		if (this.model == null) {
			this.model = new HashMap(1);
		}
		return this.model;
	}

	/**
	 * Add an object to the model.
	 * @param modelName name of the object to add to the model
	 * @param modelObject object to add to the model, may not be null
	 * @return this ViewSelection, convenient to allow usages like
	 * <code>return viewSelection.addObject("foo", bar);</code>
	 */
	public ViewSelection addObject(String modelName, Object modelObject) {
		getModel().put(modelName, modelObject);
		return this;
	}

	/**
	 * Add all entries contained in the provided map to the model.
	 * @param modelMap a map of modelName->modelObject pairs
	 * @return this ViewSelection, convenient to allow usages like
	 * <code>return viewSelection.addObject("foo", bar);</code>
	 */
	public ViewSelection addAllObjects(Map modelMap) {
		getModel().putAll(modelMap);
		return this;
	}

	/**
	 * Clear the state of this view selection. The object will be empty
	 * afterwards.
	 * @see #isEmpty()
	 */
	public void clear() {
		this.viewName = null;
		this.model = null;
	}

	/**
	 * Return whether this view selection is empty: whether it does not hold any
	 * view and does not contain a model.
	 * @see #clear()
	 */
	public boolean isEmpty() {
		return (this.viewName == null && this.model == null);
	}

	public String toString() {
		return
			new ToStringCreator(this).append("viewName", viewName)
				.append("redirect", redirect).append("model", model).toString();
	}
}