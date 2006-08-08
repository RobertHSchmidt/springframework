/**
 * 
 */
package org.springframework.webflow.execution.repository;

import java.util.HashMap;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.support.SharedMap;
import org.springframework.webflow.context.support.SharedMapDecorator;
import org.springframework.webflow.execution.repository.support.RepositoryMapLocator;

public class LocalMapLocator implements RepositoryMapLocator {
	public SharedMap source = new SharedMapDecorator(new HashMap());

	public SharedMap getMap(ExternalContext context) {
		return source;
	}

	public boolean requiresRebindOnChange() {
		return false;
	}
}