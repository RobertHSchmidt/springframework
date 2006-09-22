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
package org.springframework.webflow.engine.builder;

import org.springframework.binding.collection.MapAccessor;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.util.StringUtils;
import org.springframework.webflow.engine.NullViewSelector;
import org.springframework.webflow.engine.ViewSelector;
import org.springframework.webflow.engine.support.ApplicationViewSelector;
import org.springframework.webflow.engine.support.ExternalRedirectSelector;
import org.springframework.webflow.engine.support.FlowDefinitionRedirectSelector;
import org.springframework.webflow.execution.support.ApplicationView;
import org.springframework.webflow.execution.support.FlowExecutionRedirect;
import org.springframework.webflow.execution.support.ExternalRedirect;
import org.springframework.webflow.execution.support.FlowDefinitionRedirect;

/**
 * Converter that converts an encoded string representation of a view selector
 * into a {@link ViewSelector} object that will make selections at runtime.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"viewName" - will result in an {@link ApplicationViewSelector} that
 * returns an {@link ApplicationView} ViewSelection with the provided view name expression.</li>
 * <li>"redirect:&lt;viewName&gt;" - will result in a
 * {@link ApplicationViewSelector} that returns an {@link FlowExecutionRedirect}
 * to a flow execution URL.</li>
 * <li>"externalRedirect:&lt;url&gt;" - will result in a
 * {@link ExternalRedirectSelector} that returns an {@link ExternalRedirect} to a
 * URL.</li>
 * <li>"flowRedirect:&lt;flowId&gt;" - will result in a
 * {@link FlowDefinitionRedirectSelector} that returns a {@link FlowDefinitionRedirect} to a flow.
 * The special <code>{this}</code> refers to the current ('this') flow definition id.</li>
 * <li>"bean:&lt;id&gt;" - will result in usage of a custom
 * <code>ViewSelector</code> bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.execution.ViewSelection
 * @see org.springframework.webflow.engine.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToViewSelector extends ConversionServiceAwareConverter {

	/**
	 * The name of the context parameter indicating whether or not the view selector
	 * to create will select a view used in an end state.
	 */
	public static final String END_STATE_VIEW_FLAG_PARAMETER = "endStateView";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * to an external URL is required.
	 */
	public static final String EXTERNAL_REDIRECT_PREFIX = "externalRedirect:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * to a flow definition is requred.
	 */
	public static final String FLOW_DEFINITION_REDIRECT_PREFIX = "flowRedirect:";

	/**
	 * Prefix used when the user wants to use a ViewSelector implementation
	 * managed by a bean factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Locator to use for loading custom ViewSelector beans.
	 */
	private FlowServiceLocator flowServiceLocator;

	/**
	 * Create a new text to ViewSelector converter. Custom ViewSelector implemenations
	 * will be looked up using given service locator.
	 */
	public TextToViewSelector(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewSelector.class };
	}

	protected Object doConvert(Object source, Class targetClass, MapAccessor context) throws Exception {
		String encodedView = (String)source;
		if (!StringUtils.hasText(encodedView)) {
			return NullViewSelector.INSTANCE;
		}
		else {
			boolean endStateView = context.getRequiredBoolean(END_STATE_VIEW_FLAG_PARAMETER).booleanValue();
			return convertEncodedViewSelector(encodedView, endStateView);
		}
	}

	/**
	 * Convert given encoded view into an appropriate view selector.
	 * @param encodedView the encoded view selector
	 * @param endStateView indicates whether or not the returned view selector will be used to select
	 * a view for an end state of a flow
	 * @return the view selector
	 */
	protected ViewSelector convertEncodedViewSelector(String encodedView, boolean endStateView) {
		if (encodedView.startsWith(REDIRECT_PREFIX)) {
			String viewName = encodedView.substring(REDIRECT_PREFIX.length());
			Expression viewNameExpr = (Expression)fromStringTo(Expression.class).execute(viewName);
			if (endStateView) {
				// the flow has ended so we can't use an 'in-flow' redirect
				// just use the view as if it was an external redirect
				return new ExternalRedirectSelector(viewNameExpr);
			}
			else {
				// just show the application view using a redirect
				return new ApplicationViewSelector(viewNameExpr, true);
			}
		}
		else if (encodedView.startsWith(EXTERNAL_REDIRECT_PREFIX)) {
			String externalUrl = encodedView.substring(EXTERNAL_REDIRECT_PREFIX.length());
			Expression urlExpr = (Expression)fromStringTo(Expression.class).execute(externalUrl);
			return new ExternalRedirectSelector(urlExpr);
		}
		else if (encodedView.startsWith(FLOW_DEFINITION_REDIRECT_PREFIX)) {
			String flowRedirect = encodedView.substring(FLOW_DEFINITION_REDIRECT_PREFIX.length());
			Expression redirectExpr = (Expression)fromStringTo(Expression.class).execute(flowRedirect);
			return new FlowDefinitionRedirectSelector(redirectExpr);
		}
		else if (encodedView.startsWith(BEAN_PREFIX)) {
			String id = encodedView.substring(BEAN_PREFIX.length());
			return flowServiceLocator.getViewSelector(id);
		}
		else {
			Expression viewNameExpr = (Expression)fromStringTo(Expression.class).execute(encodedView);
			return new ApplicationViewSelector(viewNameExpr);
		}
	}
}