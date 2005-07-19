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
package org.springframework.webflow.support.convert;

import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.ConversionServiceAwareConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.CompositeStringExpression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewDescriptorCreator;

/**
 * Converter that converts an encoded string representation of a
 * view descriptor into a <code>ViewDescriptorCreator</code> that will
 * create such a view descriptor.
 * 
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"viewName" - will result in a SimpleViewDescriptorCreator that returns a ViewDescriptor 
 * with the provided view name.
 * </li>
 * <li>"redirect:&lt;viewName&gt;" - will result in a RedirectViewDescriptorCreator that returns a
 * ViewDescriptor with the provided view name and redirect flag set to true.
 * </li>
 * <li>"class:&lt;classname&gt;" - will result in instantiation and usage of a custom 
 * ViewDescriptorCreator implementation. The implementation must have a public no-arg constructor.
 * </li>
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
	 * Prefix used when the encoded view name wants to specify that
	 * a redirect is required.
	 */
	public static final String REDIRECT_PREFIX = "redirect:";

	/**
	 * Create a new text to ViewDescriptorCreator converter.
	 */
	public TextToViewDescriptorCreator() {
		super();
	}

	/**
	 * Create a new text to ViewDescriptorCreator converter. Use given
	 * conversion service for internal conversions (e.g. parsing expressions).
	 */
	public TextToViewDescriptorCreator(ConversionService conversionService) {
		super(conversionService);
	}
	
	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { ViewDescriptorCreator.class } ;
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		String encodedView = (String)source;
		if (StringUtils.hasText(encodedView)) {
			if (encodedView.startsWith(CLASS_PREFIX)) {
				Object o = newInstance(encodedView);
				Assert.isInstanceOf(ViewDescriptorCreator.class, o, "Encoded view descriptor creator is of wrong type: ");
				return (ViewDescriptorCreator)o;
			}
			else if (encodedView.startsWith(REDIRECT_PREFIX)) {
				return createRedirectViewDescriptorCreator(encodedView.substring(REDIRECT_PREFIX.length()));
			}
		}
		return createSimpleViewDescriptorCreator(encodedView);
	}
	
	/**
	 * Hook method subclasses can override to return a special simple view descriptor
	 * implementation.
	 * @param encodedView the name of the view to render
	 * @return the simple view descriptor creator
	 * @throws ConversionException when an error occurs
	 */
	protected ViewDescriptorCreator createSimpleViewDescriptorCreator(String encodedView) throws ConversionException {
		return new SimpleViewDescriptorCreator(encodedView);
	}
	
	/**
	 * Hook method sublcasses can override to return a specialized implementation
	 * of a view descriptor creator that does a redirect.
	 * @param encodedView the encoded view, without the "redirect:" prefix
	 * @return the redirecting view descriptor creator
	 * @throws ConversionException when something goes wrong
	 */
	protected ViewDescriptorCreator createRedirectViewDescriptorCreator(String encodedView) throws ConversionException {
		return new RedirectViewDescriptorCreator((Expression)fromStringTo(CompositeStringExpression.class).execute(encodedView));
	}
	
	/**
	 * Simple view descriptor creator that produces a ViewDescriptor with the same
	 * view name each time. This producer will make all model data from both
	 * flow and request scope available to the view.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public class SimpleViewDescriptorCreator implements ViewDescriptorCreator {

		/**
		 * The static view name to render.
		 */
		private String viewName;

		/**
		 * Default constructor for bean style usage.
		 */
		public SimpleViewDescriptorCreator() {
		}
		
		/**
		 * Creates a view descriptor creator that will produce view descriptors requesting that the
		 * specified view is rendered.
		 * @param viewName the view name
		 */
		public SimpleViewDescriptorCreator(String viewName) {
			setViewName(viewName);
		}
		
		/**
		 * Returns the name of the view that should be rendered.
		 */
		public String getViewName() {
			return this.viewName;
		}
		
		/**
		 * Set the name of the view that should be rendered.
		 */
		public void setViewName(String viewName) {
			this.viewName = viewName;
		}
		
		public ViewDescriptor createViewDescriptor(RequestContext context) {
			return new ViewDescriptor(getViewName(), context.getModel());
		}
		
		public String toString() {
			return new ToStringCreator(this).append("viewName", viewName).toString();
		}
	}
	
	/**
	 * View descriptor creator that creates view descriptors requesting a
	 * client side redirect. Only parameter values encoded in the view (e.g
	 * "/viewName?param0=value0&param1=value1") are exposed.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	public class RedirectViewDescriptorCreator implements ViewDescriptorCreator {
		
		private Expression expression;
		
		/**
		 * Create a new redirecting view descriptor creator that takes given
		 * list of expressions as input. The list of expressions is the parsed
		 * form (expression-tokenized) of the encoded view
		 * (e.g. "/viewName?param0=value0&param1=value1").
		 */
		public RedirectViewDescriptorCreator(Expression expression) {
			this.expression = expression;
		}

		protected Expression getExpression() {
			return expression;
		}
		
		public ViewDescriptor createViewDescriptor(RequestContext context) {
			ViewDescriptor viewDescriptor = new ViewDescriptor();
			viewDescriptor.setRedirect(true);
			String fullView = (String)expression.evaluateAgainst(context, getEvaluationContext(context));
			// the resulting fullView should look something like "/viewName?param0=value0&param1=value1"
			// now parse that and build a corresponding view descriptor
			int idx = fullView.indexOf('?');
			if (idx != -1) {
				viewDescriptor.setViewName(fullView.substring(0, idx));
				StringTokenizer parameters = new StringTokenizer(fullView.substring(idx + 1), "&");
				while (parameters.hasMoreTokens()) {
					String nameAndValue = parameters.nextToken();
					idx = nameAndValue.indexOf('=');
					if (idx !=-1) {
						viewDescriptor.addObject(nameAndValue.substring(0, idx), nameAndValue.substring(idx + 1));
					}
					else {
						viewDescriptor.addObject(nameAndValue, "");
					}
				}
			}
			else {
				// only view name specified (e.g. "/viewName")
				viewDescriptor.setViewName(fullView);
			}
			return viewDescriptor;
		}

		/**
		 * Setup the expression evaluation context.
		 */
		protected Map getEvaluationContext(RequestContext context) {
			return Collections.EMPTY_MAP;
		}
		
		public String toString() {
			return new ToStringCreator(this).append("expression", expression).toString();
		}
	}
}