package org.springframework.webflow.core;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.springframework.web.jsf.DelegatingVariableResolver;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Custom EL resolver that searches the current request context for variables to resolve. The search algorithm looks in
 * flash scope first, then flow scope, then conversation scope.
 * 
 * Suitable for use along side other variable resolvers to support EL binding expressions like "#{bean.property}" where
 * "bean" could be a property in any supported scope.
 * 
 * Consider combining use of this class with a Spring {@link DelegatingVariableResolver} to also support
 * lazy-initialized binding variables managed by a Spring application context using custom bean scopes. Also consider
 * such a Spring-backed managed bean facility as the sole-provider for centralized JSF managed bean references.
 * 
 * @author Jeremy Grelle
 */
public class FlowELResolver extends ELResolver {

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {
		if (base == null) {
			RequestContext requestContext = RequestContextHolder.getRequestContext();
			if (requestContext != null) {
				// flow execution is active: try flash/flow/conversation scope
				String name = property.toString();
				if (requestContext.getFlashScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getFlashScope().get(name).getClass();
				} else if (requestContext.getFlowScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getFlowScope().get(name).getClass();
				} else if (requestContext.getConversationScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getConversationScope().get(name).getClass();
				}
			}
		}
		return null;
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null) {
			RequestContext requestContext = RequestContextHolder.getRequestContext();
			if (requestContext != null) {
				String name = property.toString();
				if (requestContext.getFlashScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getFlashScope().get(name);
				} else if (requestContext.getFlowScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getFlowScope().get(name);
				} else if (requestContext.getConversationScope().contains(name)) {
					elContext.setPropertyResolved(true);
					return requestContext.getConversationScope().get(name);
				}
			}
		}
		return null;
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		return false;
	}

	public void setValue(ELContext elContext, Object base, Object property, Object value) {
		if (base == null) {
			RequestContext requestContext = RequestContextHolder.getRequestContext();
			if (requestContext != null) {
				String name = property.toString();
				if (requestContext.getFlashScope().contains(name)) {
					elContext.setPropertyResolved(true);
					requestContext.getFlashScope().put(name, value);
				} else if (requestContext.getFlowScope().contains(name)) {
					elContext.setPropertyResolved(true);
					requestContext.getFlowScope().put(name, value);
				} else if (requestContext.getConversationScope().contains(name)) {
					elContext.setPropertyResolved(true);
					requestContext.getConversationScope().put(name, value);
				}
			}
		}
	}

}
