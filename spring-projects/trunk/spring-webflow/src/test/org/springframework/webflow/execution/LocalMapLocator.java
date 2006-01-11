/**
 * 
 */
package org.springframework.webflow.execution;

import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.repository.ExternalMapLocator;

public class LocalMapLocator implements ExternalMapLocator {
	public Map source = new HashMap();

	public Map getMap(ExternalContext context) {
		return source;
	}
}