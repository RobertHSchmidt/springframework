package org.springframework.webflow;


/**
 * An exception indicating a request to resume a Flow execution did not occur in
 * the context of the ongoing "application transaction" associated with that
 * Flow execution.
 * 
 * @author Keith Donald
 */
public class RequestNotInTransactionException extends StateException {

	/**
	 * Creates a default instance.
	 */
	public RequestNotInTransactionException(State state) {
		super(state, "The current request is not executing in the context of an application transaction");
	}
}
