/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.support;

import java.io.Serializable;
import java.util.Collections;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Simple view selector that makes an {@link ApplicationView} selection using a
 * view name expression.
 * <p>
 * This factory will treat all attributes returned from calling
 * {@link RequestContext#getModel()} as the application model exposed to the
 * view during rendering. This is typically the union of attributes in request,
 * flow, and conversation scope.
 * <p>
 * This selector also supports setting a <i>requestConversationRedirect</i>
 * flag that will trigger a {@link ConversationRedirect} to the
 * {@link ApplicationView} at a bookmarkable conversation URL.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ApplicationViewSelector implements ViewSelector, Serializable {

	/**
	 * The view name to render.
	 */
	private Expression viewName;

	/**
	 * A flag indicating if a "redirect to conversation" should be requested.
	 * <p>
	 * Setting this to true allows you to redirect while the flow is in progress
	 * to a stable "conversation URL" that can be safely refreshed.
	 */
	private boolean requestConversationRedirect;

	/**
	 * Creates a view descriptor creator that will produce view descriptors
	 * requesting that the specified view is rendered.
	 * @param viewName the view name expression
	 */
	public ApplicationViewSelector(Expression viewName) {
		this(viewName, false);
	}

	/**
	 * Creates a view descriptor creator that will produce view descriptors
	 * requesting that the specified view is rendered.
	 * @param viewName the view name expression
	 * @param requestConversationRedirect indicates if a conversation redirect
	 * should be made
	 */
	public ApplicationViewSelector(Expression viewName, boolean requestConversationRedirect) {
		Assert.notNull(viewName, "The view name expression is required");
		this.viewName = viewName;
		this.requestConversationRedirect = requestConversationRedirect;
	}

	/**
	 * Returns the name of the view that should be rendered.
	 */
	public Expression getViewName() {
		return viewName;
	}

	/**
	 * Returns whether or not this view selection will request a "redirect to
	 * conversation".
	 */
	public boolean isRequestConversationRedirect() {
		return requestConversationRedirect;
	}

	public ViewSelection makeSelection(RequestContext context) {
		String viewName = (String)getViewName().evaluateAgainst(context, Collections.EMPTY_MAP);
		ApplicationView view = new ApplicationView(viewName, context.getModel().getMap());
		if (isRequestConversationRedirect()) {
			return new ConversationRedirect(view);
		}
		else {
			return view;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).append("requestConversationRedirect",
				requestConversationRedirect).toString();
	}
}