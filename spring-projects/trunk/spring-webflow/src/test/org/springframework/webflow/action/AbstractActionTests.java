package org.springframework.webflow.action;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

public class AbstractActionTests extends TestCase {
	private TestAbstractAction action = new TestAbstractAction();
	
	public void testInitCallback() throws Exception {
		action.afterPropertiesSet();
		assertTrue(action.initialized);
	}

	public void testInitCallbackWithException() throws Exception {
		action = new TestAbstractAction() {
			protected void initAction() {
				throw new IllegalStateException("Cannot initialize");
			}
		};
		try {
			action.afterPropertiesSet();
			fail("Should've failed initialization");
		} catch (BeanInitializationException e) {
			assertFalse(action.initialized);
		}
	}

	public void testNormalExecute() throws Exception {
		action = new TestAbstractAction() {
			protected Event doExecute(RequestContext context) throws Exception {
				return success();
			}
		};
		Event result = action.execute(new MockRequestContext());
		assertEquals("success", result.getId());
		assertTrue(result.getAttributes().size() == 0);
	}

	public void testExceptionalExecute() throws Exception {
		try {
			action.execute(new MockRequestContext());
			fail("Should've failed execute");
		} catch (IllegalStateException e) {
			
		}
	}

	public void testPreExecuteShortCircuit() throws Exception {
		action = new TestAbstractAction() {
			protected Event doPreExecute(RequestContext context) throws Exception {
				return success();
			}
		};
		Event result = action.execute(new MockRequestContext());
		assertEquals("success", result.getId());
	}
	
	public void testPostExecuteCalled() throws Exception {
		testNormalExecute();
		assertTrue(action.postExecuteCalled);
	}
	
	private class TestAbstractAction extends AbstractAction {
		private boolean initialized;

		private boolean postExecuteCalled;
		
		protected void initAction() {
			initialized = true;
		}
		
		protected Event doExecute(RequestContext context) throws Exception {
			throw new IllegalStateException("Should not be called");
		}
		
		protected void doPostExecute(RequestContext context) {
			postExecuteCalled = true;
		}
	}	
}