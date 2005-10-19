package org.springframework.webflow;

import org.springframework.core.NestedRuntimeException;

/**
 * An exception indicating a request to resume a Flow execution did not occur in
 * the context of the ongoing "application transaction" associated with that
 * Flow execution.
 * 
 * @author Keith Donald
 */
public class RequestNotInTransactionException extends NestedRuntimeException {

	/**
	 * Creates a default instance.
	 */
	public RequestNotInTransactionException() {
		super("The request is not executing in the context of an application transaction");
	}
}
