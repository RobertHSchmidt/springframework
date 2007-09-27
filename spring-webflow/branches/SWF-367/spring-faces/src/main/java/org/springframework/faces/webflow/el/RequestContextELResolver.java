package org.springframework.faces.webflow.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
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

	public Class<?> getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}

	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class<?> getType(ELContext elContext, Object base, Object property) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return RequestContext.class;
		} else if (base != null && base instanceof RequestContext) {
			elContext.setPropertyResolved(true);
			RequestContext requestContext = (RequestContext) base;
			if (FLASH_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getFlashScope().asMap().getClass();
			} else if (FLOW_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getFlowScope().asMap().getClass();
			} else if (CONVERSATION_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getConversationScope().getClass();
			} else { // perform an attribute search

				String attributeName = property.toString();
				// try flash scope
				Object value = requestContext.getFlashScope().get(attributeName);
				if (value != null) {
					return value.getClass();
				}
				// try flow scope
				value = requestContext.getFlowScope().get(attributeName);
				if (value != null) {
					return value.getClass();
				}
				// try conversation scope
				value = requestContext.getConversationScope().get(attributeName);
				if (value != null) {
					return value.getClass();
				}
				// cannot resolve as expected
				throw new PropertyNotFoundException("Readable flow request context attribute '" + attributeName
						+ "' not found in any scope (flash, flow, or conversation)");
			}

		}
		return null;
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null && REQUEST_CONTEXT_VARIABLE_NAME.equals(property)) {
			elContext.setPropertyResolved(true);
			return RequestContextHolder.getRequestContext();
		} else if (base != null && base instanceof RequestContext) {
			elContext.setPropertyResolved(true);
			RequestContext requestContext = (RequestContext) base;
			if (FLASH_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getFlashScope().asMap();
			} else if (FLOW_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getFlowScope().asMap();
			} else if (CONVERSATION_SCOPE_PROPERTY.equals(property)) {
				return requestContext.getConversationScope().asMap();
			} else { // perform an attribute search

				String attributeName = property.toString();
				// try flash scope
				Object value = requestContext.getFlashScope().get(attributeName);
				if (value != null) {
					return value;
				}
				// try flow scope
				value = requestContext.getFlowScope().get(attributeName);
				if (value != null) {
					return value;
				}
				// try conversation scope
				value = requestContext.getConversationScope().get(attributeName);
				if (value != null) {
					return value;
				}
				// cannot resolve as expected
				throw new PropertyNotFoundException("Readable flow request context attribute '" + attributeName
						+ "' not found in any scope (flash, flow, or conversation)");
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
			elContext.setPropertyResolved(true);
			if (FLASH_SCOPE_PROPERTY.equals(property) || FLOW_SCOPE_PROPERTY.equals(property)
					|| CONVERSATION_SCOPE_PROPERTY.equals(property)) {

				throw new PropertyNotWritableException("The scope maps are not replaceable.");
			} else {
				RequestContext context = (RequestContext) base;
				String attributeName = property.toString();
				if (context.getFlashScope().contains(attributeName)) {
					context.getFlashScope().put(attributeName, value);
				} else if (context.getFlowScope().contains(attributeName)) {
					context.getFlowScope().put(attributeName, value);
				} else if (context.getConversationScope().contains(attributeName)) {
					context.getConversationScope().put(attributeName, value);
				} else {
					// cannot resolve as expected
					throw new PropertyNotFoundException("Settable flow context attribute '" + attributeName
							+ "' not found in any scope (flash, flow, or conversation)");
				}
			}

		}
	}

}
