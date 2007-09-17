package org.springframework.webflow.executor;

import java.io.Serializable;

import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.execution.FlowExecutionKey;

public class FlowExecutionResult implements Serializable {

	private String encodedKey;

	private FlowException exception;

	public boolean isPaused() {
		return encodedKey != null;
	}

	public boolean isEnded() {
		return encodedKey == null && exception == null;
	}

	public boolean isException() {
		return exception != null;
	}

	public String getEncodedKey() {
		return encodedKey;
	}

	public FlowException getException() {
		return exception;
	}

	public static FlowExecutionResult getEndedResult() {
		return ENDED;
	}

	public static FlowExecutionResult getPausedResult(FlowExecutionKey key) {
		return new FlowExecutionResult(key.toString(), null);
	}

	public static FlowExecutionResult getExceptionResult(FlowException e) {
		return new FlowExecutionResult(null, e);
	}

	private FlowExecutionResult(String encodedKey, FlowException exception) {
		this.encodedKey = encodedKey;
		this.exception = exception;
	}

	private static final FlowExecutionResult ENDED = new FlowExecutionResult(null, null);

}
