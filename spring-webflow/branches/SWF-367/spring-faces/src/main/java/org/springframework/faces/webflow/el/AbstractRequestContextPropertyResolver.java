/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.webflow.el;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;

import org.springframework.webflow.execution.RequestContext;

/**
 * Base class for property resolvers that get and set request context attributes.
 * 
 * @author Keith Donald
 * @author Jeremy Grelle
 */
public abstract class AbstractRequestContextPropertyResolver extends PropertyResolver {

	/**
	 * The standard property resolver to delegate to if this one doesn't apply.
	 */
	private final PropertyResolver resolverDelegate;

	/**
	 * Creates a new request context property resolver
	 * @param resolverDelegate the resolver to delegate to when the property is not a flow execution attribute
	 */
	public AbstractRequestContextPropertyResolver(PropertyResolver resolverDelegate) {
		this.resolverDelegate = resolverDelegate;
	}

	/**
	 * Returns the property resolver this resolver delegates to if necessary.
	 */
	protected final PropertyResolver getResolverDelegate() {
		return resolverDelegate;
	}

	public Class getType(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			RequestContext context = (RequestContext) base;
			assertPropertyNameValid(property);
			return doGetAttributeType(context, (String) property);
		} else {
			return resolverDelegate.getType(base, property);
		}
	}

	public Class getType(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			// cannot access request context by index so we cannot determine type. Return null per JSF spec
			return null;
		} else {
			return resolverDelegate.getType(base, index);
		}
	}

	public Object getValue(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			RequestContext context = (RequestContext) base;
			assertPropertyNameValid(property);
			return doGetAttribute(context, (String) property);
		} else {
			return resolverDelegate.getValue(base, property);
		}
	}

	public Object getValue(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			throw new ReferenceSyntaxException("Cannot apply an index value to a flow execution");
		} else {
			return resolverDelegate.getValue(base, index);
		}
	}

	public boolean isReadOnly(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			return false;
		} else {
			return resolverDelegate.isReadOnly(base, property);
		}
	}

	public boolean isReadOnly(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			return false;
		} else {
			return resolverDelegate.isReadOnly(base, index);
		}
	}

	public void setValue(Object base, Object property, Object value) throws EvaluationException,
			PropertyNotFoundException {
		if ((base instanceof RequestContext)) {
			RequestContext execution = (RequestContext) base;
			assertPropertyNameValid(property);
			doSetAttribute(execution, (String) property, value);
		} else {
			resolverDelegate.setValue(base, property, value);
		}
	}

	public void setValue(Object base, int index, Object value) throws EvaluationException, PropertyNotFoundException {
		if (base instanceof RequestContext) {
			throw new ReferenceSyntaxException("Cannot apply an index value to a flow execution");
		} else {
			resolverDelegate.setValue(base, index, value);
		}
	}

	// helpers

	private void assertPropertyNameValid(Object property) {
		if (property == null) {
			throw new PropertyNotFoundException("The name of the flow execution attribute cannot be null");
		}
		if (!(property instanceof String)) {
			throw new PropertyNotFoundException("Flow execution attribute names must be strings but " + property
					+ " was not");
		}
		if (((String) property).length() == 0) {
			throw new PropertyNotFoundException("The name of the flow execution attribute cannot be blank");
		}
	}

	/**
	 * Gets the type of value returned by the flow execution attribute.
	 * @param context the flow request context
	 * @param attributeName the name of the attribute
	 * @return the type of value returned by the attribute
	 */
	protected abstract Class doGetAttributeType(RequestContext context, String attributeName);

	/**
	 * Gets the value of the flow execution attribute.
	 * @param context the flow request context
	 * @param attributeName the name of the attribute
	 * @return the attribute value
	 */
	protected abstract Object doGetAttribute(RequestContext context, String attributeName);

	/**
	 * Sets the value of the flow execution attribute.
	 * @param context the flow request context
	 * @param attributeName the name of the attribute
	 * @param attributeValue the attribute value
	 */
	protected abstract void doSetAttribute(RequestContext context, String attributeName, Object attributeValue);

}