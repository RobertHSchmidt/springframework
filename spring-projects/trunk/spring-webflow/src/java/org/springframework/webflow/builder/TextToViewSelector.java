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
package org.springframework.webflow.builder;

import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.CompositeStringExpression;
import org.springframework.binding.util.MapAccessor;
import org.springframework.util.StringUtils;
import org.springframework.webflow.EndState;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.support.MarkerViewSelector;
import org.springframework.webflow.support.RedirectViewSelector;
import org.springframework.webflow.support.SimpleViewSelector;

/**
 * Converter that converts an encoded string representation of a view selector
 * into a {@link ViewSelector} object that will make selections at runtime.
 * 
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"viewName" - will result in a {@link SimpleViewSelector} that returns a
 * ViewSelection with the provided view name.</li>
 * <li>"redirect:&lt;viewName&gt;" - will result in a
 * {@link RedirectViewSelector} that returns a ViewSelection with the provided
 * view name and redirect flag set to true.</li>
 * <li>"bean:&lt;id&gt;" - will result usage of a custom
 * <code>ViewSelector</code> bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.ViewSelection
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToViewSelector extends ConversionServiceAwareConverter {

	/**
	 * The name of the state context attribute; can be used to influence
	 * converter behavior.
	 */
	public static final String STATE_CONTEXT_ATTRIBUTE = "state";

	/**
	 * The name of the redirect context attribute; can be used to influence
	 * converter behavior.
	 */
	public static final String REDIRECT_CONTEXT_ATTRIBUTE = "redirect";

	/**
	 * Prefix used when the user wants to use a ViewSelector implementation
	 * managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Locator to use for loading custom ViewSelector beans.
	 */
	private FlowArtifactFactory flowArtifactFactory;

	/**
	 * Create a new text to ViewSelector converter.
	 */
	public TextToViewSelector(FlowArtifactFactory artifactLocator, ConversionService conversionService) {
		super(conversionService);
		this.flowArtifactFactory = artifactLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewSelector.class };
	}

	protected Object doConvert(Object source, Class targetClass, Map context) throws Exception {
		String encodedView = (String)source;
		if (!StringUtils.hasText(encodedView)) {
			return MarkerViewSelector.INSTANCE;
		}
		if (encodedView.startsWith(BEAN_PREFIX)) {
			return getCustomViewSelector(encodedView.substring(BEAN_PREFIX.length()));
		}
		State state = (State)context.get(STATE_CONTEXT_ATTRIBUTE);
		if (state instanceof EndState && encodedView.startsWith(REDIRECT_PREFIX)) {
			return createRedirectViewSelector(encodedView.substring(REDIRECT_PREFIX.length()));
		}
		else {
			return createSimpleViewSelector(encodedView, context);
		}
	}

	protected ViewSelector getCustomViewSelector(String id) {
		return flowArtifactFactory.getViewSelector(id);
	}

	/**
	 * Hook method subclasses can override to return a special simple view
	 * selector implementation.
	 * @param encodedView the name of the view to render
	 * @return the simple view selector
	 * @throws ConversionException when an error occurs
	 */
	protected ViewSelector createSimpleViewSelector(String encodedView, Map context) throws ConversionException {
		boolean redirect = new MapAccessor(context).getBoolean(REDIRECT_CONTEXT_ATTRIBUTE, Boolean.FALSE).booleanValue();
		return new SimpleViewSelector(encodedView, redirect);
	}

	/**
	 * Hook method sublcasses can override to return a specialized
	 * implementation of a view selector that triggers a redirect.
	 * @param encodedView the encoded view, without the "redirect:" prefix
	 * @return the redirecting view selector
	 * @throws ConversionException when something goes wrong
	 */
	protected ViewSelector createRedirectViewSelector(String encodedView) throws ConversionException {
		return new RedirectViewSelector((Expression)fromStringTo(CompositeStringExpression.class).execute(encodedView));
	}
}