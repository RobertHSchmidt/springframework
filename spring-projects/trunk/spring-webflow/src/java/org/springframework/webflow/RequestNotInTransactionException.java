package org.springframework.webflow;

import org.springframework.core.NestedRuntimeException;

public class RequestNotInTransactionException extends NestedRuntimeException {
	public RequestNotInTransactionException() {
		super("The request is not executing in the context of an application transaction");
	}
}
