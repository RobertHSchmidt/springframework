/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.webflow.mvc.view;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.format.FormatterRegistry;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.context.portlet.PortletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.portlet.PortletMvcView;
import org.springframework.webflow.mvc.servlet.ServletMvcView;

/**
 * View factory implementation that delegates to the Spring-configured view resolver chain to resolve the Spring MVC
 * view implementation to render.
 * @author Keith Donald
 */
class ViewResolvingMvcViewFactory implements ViewFactory {

	private Expression viewIdExpression;

	private ExpressionParser expressionParser;

	private FormatterRegistry formatterRegistry;

	private List viewResolvers;

	public ViewResolvingMvcViewFactory(Expression viewIdExpression, ExpressionParser expressionParser,
			FormatterRegistry formatterRegistry, List viewResolvers) {
		this.viewIdExpression = viewIdExpression;
		this.expressionParser = expressionParser;
		this.formatterRegistry = formatterRegistry;
		this.viewResolvers = viewResolvers;
	}

	public View getView(RequestContext context) {
		String viewName = (String) viewIdExpression.getValue(context);
		MvcView view;
		if (context.getExternalContext() instanceof PortletExternalContext) {
			view = new PortletMvcView(resolveView(viewName), context);
		} else {
			view = new ServletMvcView(resolveView(viewName), context);
		}
		view.setExpressionParser(expressionParser);
		view.setFormatterRegistry(formatterRegistry);
		return view;
	}

	protected org.springframework.web.servlet.View resolveView(String viewName) {
		for (Iterator it = viewResolvers.iterator(); it.hasNext();) {
			ViewResolver viewResolver = (ViewResolver) it.next();
			try {
				Locale locale = LocaleContextHolder.getLocale();
				org.springframework.web.servlet.View view = viewResolver.resolveViewName(viewName, locale);
				if (view != null) {
					return view;
				}
			} catch (Exception e) {
				throw new IllegalStateException("Exception resolving view with name '" + viewName + "'", e);
			}
		}
		return null;
	}
}