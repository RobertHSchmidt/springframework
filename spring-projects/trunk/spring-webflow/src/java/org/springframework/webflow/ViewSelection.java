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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

/**
 * Immutable Value object that provides clients with information about a logical
 * view to render and the dynamic model data necessary to render it. It is
 * expected that clients map this view selection to a physical view template for
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
 * For readers familiar with Spring MVC, this class is similiar in concept to
 * the <code>ModelAndView</code> construct. This class is provided to prevent
 * a web flow dependency on Spring MVC.
 * 
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class ViewSelection implements Serializable {

	/**
	 * Serialization version uid.
	 */
	private static final long serialVersionUID = -7048182063951237313L;

	/**
	 * Represents a null view selection, indicating a response has already been
	 * issued.
	 */
	public static final ViewSelection NULL_VIEW_SELECTION = new ViewSelection(null, null, false);

	/**
	 * The name of the view (or page or other response) to render.
	 */
	private final String viewName;

	/**
	 * A map of the data available to the view for rendering.
	 */
	private final Map model;

	/**
	 * Indicates whether or not the view should be rendered after a redirect.
	 */
	private final boolean redirect;
	
	/**
	 * Creates a new view selection given a view name and a model.
	 * @param viewName name of the view to render
	 * @param model map of model names (Strings) to model objects (Objects),
	 * model entries may not be null, but the model Map may be null if there is
	 * no model data
	 */
	public ViewSelection(String viewName, Map model, boolean redirect) {
		this.viewName = viewName;
		if (model != null) {
			this.model = new HashMap(model);
		}
		else {
			this.model = Collections.EMPTY_MAP;
		}
		this.redirect = redirect;
	}

	/**
	 * Return the view name.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * Return the model map. Never returns null. To be called by application
	 * code for modifying the model.
	 */
	public Map getModel() {
		return Collections.unmodifiableMap(model);
	}

	/**
	 * Returns whether or not a redirect is necessary when rendering this view.
	 */
	public boolean isRedirect() {
		return redirect;
	}
	
	/**
	 * Return whether this view selection is empty: whether it does not hold any
	 * view and does not contain a model.
	 */
	public boolean isNull() {
		return (viewName == null && model.isEmpty());
	}

	public boolean equals(Object o) {
		if (!(o instanceof ViewSelection)) {
			return false;
		}
		ViewSelection other = (ViewSelection)o;
		return ObjectUtils.nullSafeEquals(viewName, other.viewName) && model.equals(other.model);
	}
	
	public int hashCode() {
		return (viewName != null ? viewName.hashCode() : 0) + model.hashCode();
	}
	
	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).append("redirect", redirect).append("model",
				model).toString();
	}
}