/**
 * 
 */
package org.springframework.webflow.execution.repository;

import java.util.HashMap;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.support.SharedAttributeMapLocator;
import org.springframework.webflow.context.support.SharedMap;
import org.springframework.webflow.context.support.SharedMapDecorator;

public class LocalMapLocator implements SharedAttributeMapLocator {
	public SharedMap source = new SharedMapDecorator(new HashMap());

	public SharedMap getMap(ExternalContext context) {
		return source;
	}

	public boolean attributesRequireRebindingOnChange() {
		return false;
	}
}