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
package org.springframework.webflow.builder;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.EndState;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.support.ApplicationViewSelection;
import org.springframework.webflow.support.ExternalRedirect;
import org.springframework.webflow.test.MockRequestContext;

/**
 * Test case for TextToViewDescriptorCreator.
 * 
 * @author Erwin Vervaet
 */
public class TextToViewSelectorTests extends TestCase {

	private TextToViewSelector converter;
	
	public void setUp() {
		DefaultFlowArtifactFactory flowArtifactFactory = new DefaultFlowArtifactFactory();
		converter = new TextToViewSelector(flowArtifactFactory);
		converter.setConversionService(flowArtifactFactory.getConversionService());
	}

	public void testStaticView() {
		ViewSelector selector = (ViewSelector)converter.convert("myView");
		RequestContext context = getRequestContext();
		ApplicationViewSelection view = (ApplicationViewSelection)selector.makeSelection(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}

	public void testRedirectView() {
		Flow flow = new Flow("test");
		EndState endState = new EndState(flow, "id");
		Map cContext = new HashMap();
		cContext.put(TextToViewSelector.STATE_CONTEXT_ATTRIBUTE, endState);
		ViewSelector selector = (ViewSelector)converter
				.convert("redirect:myView?foo=${flowScope.foo}&bar=${requestScope.oven}", cContext);
		RequestContext context = getRequestContext();
		ExternalRedirect view = (ExternalRedirect)selector.makeSelection(context);
		assertEquals("myView?foo=bar&bar=mit", view.getUrl());
	}

	private RequestContext getRequestContext() {
		MockRequestContext ctx = new MockRequestContext();
		ctx.getFlowScope().put("foo", "bar");
		ctx.getFlowScope().put("bar", "car");
		ctx.getRequestScope().put("oven", "mit");
		ctx.getRequestScope().put("cat", "woman");
		ctx.getFlowScope().put("boo", new Integer(3));
		ctx.setLastEvent(new Event(this, "sample"));
		return ctx;
	}
}