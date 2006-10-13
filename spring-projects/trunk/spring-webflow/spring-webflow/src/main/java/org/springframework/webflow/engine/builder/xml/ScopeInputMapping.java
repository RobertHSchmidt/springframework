package org.springframework.webflow.engine.builder.xml;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.MappingContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

public class ScopeInputMapping implements AttributeMapper {

	private String attributeName;
	
	private ScopeType scopeType;
	
	public ScopeInputMapping(String attributeName, ScopeType scopeType) {
		this.attributeName = attributeName;
		this.scopeType = scopeType;
	}

	public void map(Object source, Object target, MappingContext context) {
		AttributeMap inputMap = (AttributeMap)source;
		RequestContext requestContext = (RequestContext)target;
		Object attributeValue = inputMap.get(attributeName);
		scopeType.getScope(requestContext).put(attributeName, attributeValue);
	}
}
