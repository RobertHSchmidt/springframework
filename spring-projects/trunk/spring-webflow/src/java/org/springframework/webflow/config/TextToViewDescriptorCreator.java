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
package org.springframework.webflow.config;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.CompositeStringExpression;
import org.springframework.util.StringUtils;
import org.springframework.webflow.ViewDescriptorCreator;

/**
 * Converter that converts an encoded string representation of a view descriptor
 * into a <code>ViewDescriptorCreator</code> that will create such a view
 * descriptor.
 * 
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"viewName" - will result in a SimpleViewDescriptorCreator that returns a
 * ViewDescriptor with the provided view name.</li>
 * <li>"redirect:&lt;viewName&gt;" - will result in a
 * RedirectViewDescriptorCreator that returns a ViewDescriptor with the provided
 * view name and redirect flag set to true.</li>
 * <li>"bean:&lt;id&gt;" - will result usage of a custom ViewDescriptorCreator
 * bean implementation.</li>
 * </ul>
 * 
 * @see org.springframework.webflow.ViewDescriptor
 * @see org.springframework.webflow.ViewDescriptorCreator
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToViewDescriptorCreator extends ConversionServiceAwareConverter {

	/**
	 * Prefix used when the user wants to use a ViewDescriptorCreator managed by
	 * a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Prefix used when the encoded view name wants to specify that a redirect
	 * is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Locator to use for loading custom ViewDescriptorCreator beans.
	 */
	private FlowArtifactLocator artifactLocator;

	/**
	 * Create a new text to ViewDescriptorCreator converter.
	 */
	public TextToViewDescriptorCreator(FlowArtifactLocator artifactLocator, ConversionService conversionService) {
		super(conversionService);
		this.artifactLocator = artifactLocator;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewDescriptorCreator.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedView = (String)source;
		if (StringUtils.hasText(encodedView)) {
			if (encodedView.startsWith(REDIRECT_PREFIX)) {
				return createRedirectViewDescriptorCreator(encodedView.substring(REDIRECT_PREFIX.length()));
			}
			else if (encodedView.startsWith(BEAN_PREFIX)) {
				return artifactLocator.getViewDescriptorCreator(encodedView.substring(BEAN_PREFIX.length()));
			}
		}
		return createSimpleViewDescriptorCreator(encodedView);
	}

	/**
	 * Hook method subclasses can override to return a special simple view
	 * descriptor implementation.
	 * @param encodedView the name of the view to render
	 * @return the simple view descriptor creator
	 * @throws ConversionException when an error occurs
	 */
	protected ViewDescriptorCreator createSimpleViewDescriptorCreator(String encodedView) throws ConversionException {
		return new SimpleViewDescriptorCreator(encodedView);
	}

	/**
	 * Hook method sublcasses can override to return a specialized
	 * implementation of a view descriptor creator that does a redirect.
	 * @param encodedView the encoded view, without the "redirect:" prefix
	 * @return the redirecting view descriptor creator
	 * @throws ConversionException when something goes wrong
	 */
	protected ViewDescriptorCreator createRedirectViewDescriptorCreator(String encodedView) throws ConversionException {
		return new RedirectViewDescriptorCreator((Expression)fromStringTo(CompositeStringExpression.class).execute(
				encodedView));
	}
}