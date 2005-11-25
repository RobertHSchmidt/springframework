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
package org.springframework.webflow.config.support;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.CompositeStringExpression;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ViewSelector;

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
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Create a new text to ViewSelector converter.
	 */
	public TextToViewSelector(ConversionService conversionService) {
		super(conversionService);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewSelector.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedView = (String)source;
		if (StringUtils.hasText(encodedView)) {
			if (encodedView.startsWith(REDIRECT_PREFIX)) {
				return createRedirectViewSelector(encodedView.substring(REDIRECT_PREFIX.length()));
			}
		}
		return createSimpleViewSelector(encodedView);
	}

	/**
	 * Hook method subclasses can override to return a special simple view
	 * selector implementation.
	 * @param encodedView the name of the view to render
	 * @return the simple view selector
	 * @throws ConversionException when an error occurs
	 */
	protected ViewSelector createSimpleViewSelector(String encodedView) throws ConversionException {
		return new SimpleViewSelector(encodedView);
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