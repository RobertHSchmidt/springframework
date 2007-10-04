package org.springframework.webflow.core;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Custom EL resolver that resolves to a thread-bound RequestContext object for binding expressions prefixed with a
 * {@link #REQUEST_CONTEXT_VARIABLE_NAME}. For instance "#{requestContext.conversationScope.myProperty}".
 * 
 * Supports resolving all scopes as java.util.Maps: "flowScope", "conversationScope", and "flashScope". Also supports
 * attribute searching when no scope prefix is specified, i.e. "#{requestContext.myProperty}". The search order is
 * flash, flow, conversation.
 * 
 * @author Jeremy Grelle
 */
public class RequestContextELResolver extends ELResolver {

	/**
	 * Name of the request context variable.
	 */
	public static final String REQUEST_CONTEXT_VARIABLE_NAME = "requestContext";

	/**
	 * The name of the special flash scope execution property.
	 */
	private static final String FLASH_SCOPE_PROPERTY = "flashScope";

	/**
	 * The name of the special flow scope context property.
	 */
	private static final String FLOW_SCOPE_PROPERTY = "flowScope";

	/**
	 * The name of the special conversation scope context property.
	 */
	private static final String CONVERSATION_SCOPE_PROPERTY = "conversationScope";

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return RequestContext.class;
		} else if (base != null && base instanceof RequestContext) {
			RequestContext requestContext = (RequestContext) base;
			if (FLASH_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getFlashScope().asMap().getClass();
			} else if (FLOW_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getFlowScope().asMap().getClass();
			} else if (CONVERSATION_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getConversationScope().getClass();
			} else { // perform an attribute search

				String attributeName = property.toString();
				// try flash scope
				Object value = requestContext.getFlashScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value.getClass();
				}
				// try flow scope
				value = requestContext.getFlowScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value.getClass();
				}
				// try conversation scope
				value = requestContext.getConversationScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value.getClass();
				}
			}
		}
		return null;
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return RequestContextHolder.getRequestContext();
		} else if (base != null && base instanceof RequestContext) {
			RequestContext requestContext = (RequestContext) base;
			if (FLASH_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getFlashScope().asMap();
			} else if (FLOW_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getFlowScope().asMap();
			} else if (CONVERSATION_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				return requestContext.getConversationScope().asMap();
			} else { // perform an attribute search

				String attributeName = property.toString();
				// try flash scope
				Object value = requestContext.getFlashScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value;
				}
				// try flow scope
				value = requestContext.getFlowScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value;
				}
				// try conversation scope
				value = requestContext.getConversationScope().get(attributeName);
				if (value != null) {
					elContext.setPropertyResolved(true);
					return value;
				}
			}
		}
		return null;
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return true;
		} else if (base != null && base instanceof RequestContext) {
			elContext.setPropertyResolved(true);
			if (FLASH_SCOPE_PROPERTY.equals(property) || FLOW_SCOPE_PROPERTY.equals(property)
					|| CONVERSATION_SCOPE_PROPERTY.equals(property)) {

				return true;
			}
		}
		return false;
	}

	public void setValue(ELContext elContext, Object base, Object property, Object value) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			throw new PropertyNotWritableException("The RequestContext cannot be set with an expression.");
		} else if (base != null && base instanceof RequestContext) {
			if (FLASH_SCOPE_PROPERTY.equals(property) || FLOW_SCOPE_PROPERTY.equals(property)
					|| CONVERSATION_SCOPE_PROPERTY.equals(property)) {
				elContext.setPropertyResolved(true);
				throw new PropertyNotWritableException("The scope maps are not replaceable.");
			} else {
				RequestContext context = (RequestContext) base;
				String attributeName = property.toString();
				if (context.getFlashScope().contains(attributeName)) {
					elContext.setPropertyResolved(true);
					context.getFlashScope().put(attributeName, value);
				} else if (context.getFlowScope().contains(attributeName)) {
					elContext.setPropertyResolved(true);
					context.getFlowScope().put(attributeName, value);
				} else if (context.getConversationScope().contains(attributeName)) {
					elContext.setPropertyResolved(true);
					context.getConversationScope().put(attributeName, value);
				}
			}

		}
	}

}
