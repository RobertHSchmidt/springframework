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
package org.springframework.webflow.execution.internal.support;

import java.io.Serializable;
import java.util.Collections;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.engine.ViewSelector;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.execution.support.FlowExecutionRedirect;

/**
 * Simple view selector that makes an {@link ApplicationView} selection using a
 * view name expression.
 * <p>
 * This factory will treat all attributes returned from calling
 * {@link RequestContext#getModel()} as the application model exposed to the
 * view during rendering. This is typically the union of attributes in request,
 * flow, and conversation scope.
 * <p>
 * This selector also supports setting a <i>redirect</i> flag that can be
 * used to trigger a redirect to the {@link ApplicationView} at a bookmarkable
 * URL.
 * 
 * @see org.springframework.webflow.execution.support.ApplicationView
 * @see org.springframework.webflow.execution.support.FlowExecutionRedirect
 * @see org.springframework.webflow.execution.ViewSelection#NULL_VIEW
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
	 * A flag indicating if a redirect to the selected application view should
	 * be requested.
	 * <p>
	 * Setting this allows you to redirect while the flow is in progress to a
	 * stable URL that can be safely refreshed.
	 */
	private boolean redirect;

	/**
	 * Creates a application view selector that will make application view
	 * selections requesting that the specified view be rendered.
	 * @param viewName the view name expression
	 */
	public ApplicationViewSelector(Expression viewName) {
		this(viewName, false);
	}

	/**
	 * Creates a application view selector that will make application view
	 * selections requesting that the specified view be rendered.
	 * @param viewName the view name expression
	 * @param redirect indicates if a redirect to the view should be
	 * initiated
	 */
	public ApplicationViewSelector(Expression viewName, boolean redirect) {
		Assert.notNull(viewName, "The view name expression is required");
		this.viewName = viewName;
		this.redirect = redirect;
	}

	/**
	 * Returns the name of the view that should be rendered.
	 */
	public Expression getViewName() {
		return viewName;
	}

	public boolean isEntrySelectionRenderable(RequestContext context) {
		return !redirect;
	}
	
	public ViewSelection makeEntrySelection(RequestContext context) {
		if (redirect) {
			return FlowExecutionRedirect.INSTANCE;
		}
		else {
			return makeRefreshSelection(context);
		}
	}

	public ViewSelection makeRefreshSelection(RequestContext context) {
		String viewName = resolveViewName(context);
		if (!StringUtils.hasText(viewName)) {
			return ViewSelection.NULL_VIEW;
		}
		return createApplicationView(viewName, context);
	}

	/**
	 * Resolves the application view name from the request context.
	 * @param context the context
	 * @return the view name
	 */
	protected String resolveViewName(RequestContext context) {
		return (String)getViewName().evaluateAgainst(context, Collections.EMPTY_MAP);
	}

	/**
	 * Creates the application view selection.
	 * @param viewName the resolved view name
	 * @param context the context
	 * @return the application view
	 */
	protected ApplicationView createApplicationView(String viewName, RequestContext context) {
		return new ApplicationView(viewName, context.getModel().getMap());
	}
	
	public String toString() {
		return new ToStringCreator(this).append("viewName", viewName).append("redirect", redirect).toString();
	}
}