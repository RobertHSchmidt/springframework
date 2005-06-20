package org.springframework.webflow.config;

import junit.framework.TestCase;

import org.springframework.binding.support.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.ViewDescriptorCreator;
import org.springframework.webflow.test.MockRequestContext;

public class TextToViewDescriptorCreatorTests extends TestCase {
	public void testStaticView() {
		TextToViewDescriptorCreator converter = new TextToViewDescriptorCreator();
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("myView");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
		assertEquals("myView", view.getViewName());
		assertEquals(5, view.getModel().size());
	}
	
	public void testRedirectView() {
		TextToViewDescriptorCreator converter = new TextToViewDescriptorCreator();
		ViewDescriptorCreator creator = (ViewDescriptorCreator)converter.convert("redirect:myView?foo=${flowScope.foo}&bar=${requestScope.oven}");
		RequestContext context = getRequestContext();
		ViewDescriptor view = creator.createViewDescriptor(context);
		assertEquals("myView", view.getViewName());
		assertEquals(2, view.getModel().size());
		Assert.attributeEquals(view, "foo", "bar");
		Assert.attributeEquals(view, "bar", "mit");
	}
	
	public void testCustom() {
		
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
}
