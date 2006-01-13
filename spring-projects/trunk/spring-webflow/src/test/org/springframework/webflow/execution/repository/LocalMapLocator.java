/**
 * 
 */
package org.springframework.webflow.execution.repository;

import java.util.HashMap;

import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.ExternalContext.SharedMap;
import org.springframework.webflow.context.SharedMapDecorator;

public class LocalMapLocator implements SharedMapLocator {
	public SharedMap source = new SharedMapDecorator(new HashMap());

	public SharedMap getMap(ExternalContext context) {
		return source;
	}
}