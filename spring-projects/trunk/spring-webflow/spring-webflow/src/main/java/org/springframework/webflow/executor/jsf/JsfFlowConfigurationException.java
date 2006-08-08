package org.springframework.webflow.executor.jsf;

import org.springframework.webflow.core.FlowException;

/**
 * Thrown when there is a configuration error with SWF within a JSF environment.
 * @author Keith Donald
 */
public class JsfFlowConfigurationException extends FlowException {
	public JsfFlowConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
