package org.springframework.webflow.mvc.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ContextResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.test.GeneratedFlowExecutionKey;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

public class MvcViewFactoryTests extends TestCase {
	private MvcViewFactoryCreator creator;
	private StaticApplicationContext applicationContext;

	protected void setUp() {
		creator = new MvcViewFactoryCreator();
		applicationContext = new StaticApplicationContext();
		applicationContext.refresh();
	}

	public void testGetViewNoFlowApplicationContext() {
		Expression viewId = new StaticExpression("flowrelativeview.jsp");
		InternalFlowResourceMvcViewFactory factory = new InternalFlowResourceMvcViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		try {
			factory.getView(context);
			fail("Expected illegal state");
		} catch (IllegalStateException e) {
			// expected;
		}
	}

	public void testGetViewNoFlowApplicationContextAbsolutePath() {
		Expression viewId = new StaticExpression("/absoluteview.jsp");
		InternalFlowResourceMvcViewFactory factory = new InternalFlowResourceMvcViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		assertNotNull(factory.getView(context));
	}

	public void testNoResolversGetResource() throws Exception {
		ResourceLoader viewResourceLoader = new ResourceLoader() {
			public ClassLoader getClassLoader() {
				return ClassUtils.getDefaultClassLoader();
			}

			public Resource getResource(String name) {
				return new TestContextResource("/parent/" + name);
			}
		};
		applicationContext.setResourceLoader(viewResourceLoader);
		Expression viewId = new StaticExpression("myview.jsp");
		ViewFactory viewFactory = creator.createViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		context.getRootFlow().setApplicationContext(applicationContext);
		MockExternalContext externalContext = new MockExternalContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		externalContext.setNativeRequest(request);
		externalContext.setNativeResponse(response);
		context.setExternalContext(externalContext);
		context.getMockFlowExecutionContext().setKey(new GeneratedFlowExecutionKey());
		View view = viewFactory.getView(context);
		assertEquals(false, view.hasFlowEvent());
		view.render();
		assertEquals("/parent/myview.jsp", response.getForwardedUrl());
	}

	public void testViewResolversGetResource() throws Exception {
		MockViewResolver viewResolver = new MockViewResolver("myview");
		creator.setViewResolvers(Collections.singletonList(viewResolver));
		Expression viewId = new StaticExpression("myview");
		ViewFactory viewFactory = creator.createViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		MockExternalContext externalContext = new MockExternalContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		externalContext.setNativeRequest(request);
		externalContext.setNativeResponse(response);
		context.setExternalContext(externalContext);
		context.getMockFlowExecutionContext().setKey(new GeneratedFlowExecutionKey());
		View view = viewFactory.getView(context);
		assertEquals(false, view.hasFlowEvent());
		view.render();
		assertEquals("myview", response.getForwardedUrl());
	}

	public void testRestoreView() throws Exception {
		ResourceLoader viewResourceLoader = new ResourceLoader() {
			public ClassLoader getClassLoader() {
				return ClassUtils.getDefaultClassLoader();
			}

			public Resource getResource(String name) {
				return new TestContextResource("/parent/" + name);
			}
		};
		applicationContext.setResourceLoader(viewResourceLoader);
		Expression viewId = new StaticExpression("myview.jsp");
		ViewFactory viewFactory = creator.createViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		context.getRootFlow().setApplicationContext(applicationContext);
		MockExternalContext externalContext = new MockExternalContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		externalContext.putRequestParameter("_eventId", "foo");
		externalContext.setNativeRequest(request);
		externalContext.setNativeResponse(response);
		context.setExternalContext(externalContext);
		context.getMockFlowExecutionContext().setKey(new GeneratedFlowExecutionKey());
		View view = viewFactory.getView(context);
		view.processUserEvent();
		assertEquals(true, view.hasFlowEvent());
		Event e = view.getFlowEvent();
		assertEquals(view, e.getSource());
		assertEquals("foo", e.getId());
		view.render();
		assertEquals("/parent/myview.jsp", response.getForwardedUrl());
	}

	public void testRestoreViewButtonEventIdFormat() throws Exception {
		ResourceLoader viewResourceLoader = new ResourceLoader() {
			public ClassLoader getClassLoader() {
				return ClassUtils.getDefaultClassLoader();
			}

			public Resource getResource(String name) {
				return new TestContextResource("/parent/" + name);
			}
		};
		applicationContext.setResourceLoader(viewResourceLoader);
		Expression viewId = new StaticExpression("myview.jsp");
		ViewFactory viewFactory = creator.createViewFactory(viewId, null, null);
		MockRequestContext context = new MockRequestContext();
		context.getRootFlow().setApplicationContext(applicationContext);
		MockExternalContext externalContext = new MockExternalContext();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		externalContext.putRequestParameter("_eventId_foo", "true");
		externalContext.setNativeRequest(request);
		externalContext.setNativeResponse(response);
		context.setExternalContext(externalContext);
		context.getMockFlowExecutionContext().setKey(new GeneratedFlowExecutionKey());
		View view = viewFactory.getView(context);
		view.processUserEvent();
		assertEquals(true, view.hasFlowEvent());
		Event e = view.getFlowEvent();
		assertEquals(view, e.getSource());
		assertEquals("foo", e.getId());
		view.render();
		assertEquals("/parent/myview.jsp", response.getForwardedUrl());
	}

	private static class MockViewResolver implements ViewResolver {

		private String expectedViewName;

		public MockViewResolver(String expectedViewName) {
			this.expectedViewName = expectedViewName;
		}

		public org.springframework.web.servlet.View resolveViewName(String viewName, Locale arg1) throws Exception {
			assertEquals(expectedViewName, viewName);
			return new MockView();
		}

		class MockView implements org.springframework.web.servlet.View {
			boolean renderCalled;

			public String getContentType() {
				return "text/html";
			}

			public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
				request.getRequestDispatcher(expectedViewName).forward(request, response);
				renderCalled = true;
			}
		}

	}

	private static class TestContextResource extends AbstractResource implements ContextResource {
		private String path;

		public TestContextResource(String path) {
			this.path = path;
		}

		public String getDescription() {
			return "test context resource";
		}

		public InputStream getInputStream() throws IOException {
			throw new UnsupportedOperationException("Auto-generated method stub");
		}

		public String getPathWithinContext() {
			return path;
		}
	}
}