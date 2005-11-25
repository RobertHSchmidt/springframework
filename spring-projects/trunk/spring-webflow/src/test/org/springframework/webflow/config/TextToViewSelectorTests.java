/*
 * Copyright 2002-2004 the original author or authors.
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

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.binding.support.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.config.TextToViewSelector;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for TextToViewDescriptorCreator.
 * 
 * @author Erwin Vervaet
 */
public class TextToViewSelectorTests extends TestCase {

	private TextToViewSelector converter = new TextToViewSelector(
			new FlowArtifactFactory(new DefaultListableBeanFactory()), new DefaultConversionService());

	public void testStaticView() {
		ViewSelector selector = (ViewSelector)converter.convert("myView");
		RequestContext context = getRequestContext();
		ViewSelection view = selector.makeSelection(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}

	public void testRedirectView() {
		ViewSelector selector = (ViewSelector)converter
				.convert("redirect:myView?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		ViewSelection view = selector.makeSelection(context);
		assertEquals("myView", view.getViewName());
		assertEquals(2, view.getModel().size());
		Assert.attributeEquals(view, "foo", "bar");
		Assert.attributeEquals(view, "bar", "mit");
	}

	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.getFlowScope().setAttribute("foo", "bar");
		ctx.getFlowScope().setAttribute("bar", "car");
		ctx.getRequestScope().setAttribute("oven", "mit");
		ctx.getRequestScope().setAttribute("cat", "woman");
		ctx.getFlowScope().setAttribute("boo", new Integer(3));
		ctx.setLastEvent(new Event(this, "sample"));
		return ctx;
	}

	public void testCreateRedirectViewDescriptorCreator() {
		RequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("foo", "foo");
		context.getFlowScope().setAttribute("bar", "bar");

		ViewSelection selection = converter.createRedirectViewSelector("/viewName")
				.makeSelection(context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(0, selection.getModel().size());

		selection = converter.createRedirectViewSelector("").makeSelection(context);
		assertEquals("", selection.getViewName());
		assertEquals(0, selection.getModel().size());

		selection = converter.createRedirectViewSelector(null).makeSelection(context);
		assertEquals("", selection.getViewName());
		assertEquals(0, selection.getModel().size());

		selection = converter.createRedirectViewSelector("/viewName?").makeSelection(context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(0, selection.getModel().size());

		selection = converter.createRedirectViewSelector("/viewName?param0=").makeSelection(
				context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(1, selection.getModel().size());
		assertEquals("", selection.getModel().get("param0"));

		selection = converter.createRedirectViewSelector("/viewName?=value0").makeSelection(
				context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(1, selection.getModel().size());
		assertEquals("value0", selection.getModel().get(""));

		selection = converter.createRedirectViewSelector("/viewName?param0=value0").makeSelection(
				context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(1, selection.getModel().size());
		assertEquals("value0", selection.getModel().get("param0"));

		selection = converter.createRedirectViewSelector("/viewName?param0=${flowScope.foo}")
				.makeSelection(context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(1, selection.getModel().size());
		assertEquals("foo", selection.getModel().get("param0"));

		selection = converter.createRedirectViewSelector(
				"/viewName?param0=${flowScope.foo}&param1=${flowScope.bar}").makeSelection(context);
		assertEquals("/viewName", selection.getViewName());
		assertEquals(2, selection.getModel().size());
		assertEquals("foo", selection.getModel().get("param0"));
		assertEquals("bar", selection.getModel().get("param1"));
	}
}