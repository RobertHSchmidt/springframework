package org.springframework.webflow.engine;

import junit.framework.TestCase;

import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewSelection;
import org.springframework.webflow.execution.support.ApplicationView;

/**
 * Unit tests for {@link org.springframework.webflow.engine.FlowExecutionExceptionHandler} related code.
 * 
 * @author Erwin Vervaet
 */
public class StateExceptionHandlerTests extends TestCase {
	
	public void testHandleException() {
		FlowExecutionExceptionHandlerSet handlerSet = new FlowExecutionExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(NullPointerException.class, new ApplicationView("NOK", null)));
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, new ApplicationView("OK", null)));
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, new ApplicationView("NOK", null)));
		
		FlowExecutionException testException = new FlowExecutionException("flowId", "stateId", "Test");
		assertNotNull(
				"First handler should have been ignored since it does not handle StateException",
				handlerSet.handleException(testException, null));
		assertEquals(
				"Third handler should not have been reached since second handler handles excpetion and returns not-null",
				"OK", ((ApplicationView)handlerSet.handleException(testException, null)).getViewName());
	}
	
	public void testHandleExceptionWithNulls() {
		FlowExecutionExceptionHandlerSet handlerSet = new FlowExecutionExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, null));
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, new ApplicationView("OK", null)));
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, new ApplicationView("NOK", null)));
		
		FlowExecutionException testException = new FlowExecutionException("flowId", "stateId", "Test");
		assertNotNull(
				"First handler should have been ignored since it return null",
				handlerSet.handleException(testException, null));
		assertEquals(
				"Third handler should not have been reached since second handler handles excpetion and returns not-null",
				"OK", ((ApplicationView)handlerSet.handleException(testException, null)).getViewName());
	}
	
	public void testHandleExceptionNoMatch() {
		FlowExecutionExceptionHandlerSet handlerSet = new FlowExecutionExceptionHandlerSet();
		
		handlerSet.add(new TestStateExceptionHandler(FlowExecutionException.class, null));
		handlerSet.add(new TestStateExceptionHandler(NullPointerException.class, new ApplicationView("NOK", null)));
		
		FlowExecutionException testException = new FlowExecutionException("flowId", "stateId", "Test");
		assertNull(
				"First handler should have been ignored since it return null, " +
				"second handler should have been ignored since it does not handle the exception",
				handlerSet.handleException(testException, null));
	}
	
	/**
	 * State exception handler used in tests.
	 */
	public static class TestStateExceptionHandler implements FlowExecutionExceptionHandler {
		
		private Class typeToHandle;
		private ViewSelection handleResult;
		
		public TestStateExceptionHandler(Class typeToHandle, ViewSelection handleResult) {
			this.typeToHandle = typeToHandle;
			this.handleResult = handleResult;
		}
		
		public boolean handles(FlowExecutionException exception) {
			return typeToHandle.isInstance(exception);
		}
		
		public ViewSelection handle(FlowExecutionException exception, RequestControlContext context) {
			return handleResult;
		}
	}

}
