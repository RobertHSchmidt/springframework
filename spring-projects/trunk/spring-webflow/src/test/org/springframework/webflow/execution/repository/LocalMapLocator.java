/**
 * 
 */
package org.springframework.webflow.execution.repository;

import java.util.HashMap;

import org.springframework.binding.map.SharedMap;
import org.springframework.binding.map.SharedMapDecorator;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.execution.repository.support.SharedMapFlowExecutionRepositoryFactory.SharedMapLocator;

public class LocalMapLocator implements SharedMapLocator {
	public SharedMap source = new SharedMapDecorator(new HashMap());

	public SharedMap getMap(ExternalContext context) {
		return source;
	}
}