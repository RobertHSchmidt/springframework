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
package org.springframework.webflow.support;

import junit.framework.TestCase;

import org.springframework.binding.support.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.support.FlowConversionService;
import org.springframework.webflow.support.TextToViewDescriptorCreator;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for TextToViewDescriptorCreator.
 * 
 * @author Erwin Vervaet
 */
public class TextToViewDescriptorCreatorTests extends TestCase {

	private TextToViewDescriptorCreator converter = new TextToViewDescriptorCreator(new FlowConversionService());
	
	public void testStaticView() {
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("myView");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}
	
	public void testRedirectView() {
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("redirect:myView?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
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
		
		ViewDescriptor viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(0, viewDescriptor.getModel().size());
		
		viewDescriptor = converter.createRedirectViewDescriptorCreator("").createViewDescriptor(context);
		assertEquals("", viewDescriptor.getViewName());
		assertEquals(0, viewDescriptor.getModel().size());

		viewDescriptor = converter.createRedirectViewDescriptorCreator(null).createViewDescriptor(context);
		assertEquals("", viewDescriptor.getViewName());
		assertEquals(0, viewDescriptor.getModel().size());

		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(0, viewDescriptor.getModel().size());

		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?param0=").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(1, viewDescriptor.getModel().size());
		assertEquals("", viewDescriptor.getModel().get("param0"));

		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?=value0").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(1, viewDescriptor.getModel().size());
		assertEquals("value0", viewDescriptor.getModel().get(""));

		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?param0=value0").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(1, viewDescriptor.getModel().size());
		assertEquals("value0", viewDescriptor.getModel().get("param0"));
		
		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?param0=${flowScope.foo}").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(1, viewDescriptor.getModel().size());
		assertEquals("foo", viewDescriptor.getModel().get("param0"));
		
		viewDescriptor = converter.createRedirectViewDescriptorCreator("/viewName?param0=${flowScope.foo}&param1=${flowScope.bar}").createViewDescriptor(context);
		assertEquals("/viewName", viewDescriptor.getViewName());
		assertEquals(2, viewDescriptor.getModel().size());
		assertEquals("foo", viewDescriptor.getModel().get("param0"));
		assertEquals("bar", viewDescriptor.getModel().get("param1"));
	}

}
