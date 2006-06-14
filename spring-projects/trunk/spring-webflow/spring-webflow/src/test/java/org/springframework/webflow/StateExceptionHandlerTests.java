package org.springframework.webflow;

import org.springframework.webflow.support.ApplicationView;

import junit.framework.TestCase;

/**
 * Unit tests for {@link org.springframework.webflow.StateExceptionHandler} related code.
 * 
 * @author Erwin Vervaet
 */
public class StateExceptionHandlerTests extends TestCase {
	
	public void testHandleException() {
		StateExceptionHandlerSet handlerSet = new StateExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(NullPointerException.class, new ApplicationView("NOK", null)));
		handlerSet.add(new TestStateExceptionHandler(StateException.class, new ApplicationView("OK", null)));
		handlerSet.add(new TestStateExceptionHandler(StateException.class, new ApplicationView("NOK", null)));
		
		StateException testException = new StateException(null, "Test");
		assertNotNull(
				"First handler should have been ignored since it does not handle StateException",
				handlerSet.handleException(testException, null));
		assertEquals(
				"Third handler should not have been reached since second handler handles excpetion and returns not-null",
				"OK", ((ApplicationView)handlerSet.handleException(testException, null)).getViewName());
	}
	
	public void testHandleExceptionWithNulls() {
		StateExceptionHandlerSet handlerSet = new StateExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(StateException.class, null));
		handlerSet.add(new TestStateExceptionHandler(StateException.class, new ApplicationView("OK", null)));
		handlerSet.add(new TestStateExceptionHandler(StateException.class, new ApplicationView("NOK", null)));
		
		StateException testException = new StateException(null, "Test");
		assertNotNull(
				"First handler should have been ignored since it return null",
				handlerSet.handleException(testException, null));
		assertEquals(
				"Third handler should not have been reached since second handler handles excpetion and returns not-null",
				"OK", ((ApplicationView)handlerSet.handleException(testException, null)).getViewName());
	}
	
	public void testHandleExceptionNoMatch() {
		StateExceptionHandlerSet handlerSet = new StateExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(StateException.class, null));
		handlerSet.add(new TestStateExceptionHandler(NullPointerException.class, new ApplicationView("NOK", null)));
		
		StateException testException = new StateException(null, "Test");
		assertNull(
				"First handler should have been ignored since it return null, " +
				"second handler should have been ignored since it does not handle the exception",
				handlerSet.handleException(testException, null));
	}
	
	/**
	 * State exception handler used in tests.
	 */
	public static class TestStateExceptionHandler implements StateExceptionHandler {
		
		private Class typeToHandle;
		private ViewSelection handleResult;
		
		public TestStateExceptionHandler(Class typeToHandle, ViewSelection handleResult) {
			this.typeToHandle = typeToHandle;
			this.handleResult = handleResult;
		}
		
		public boolean handles(StateException exception) {
			return typeToHandle.isInstance(exception);
		}
		
		public ViewSelection handle(StateException exception, RequestControlContext context) {
			return handleResult;
		}
	}

}
