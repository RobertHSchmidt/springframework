/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.webflow.jsf;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;
import org.springframework.webflow.execution.FlowExecution;

/**
 * TODO: doc
 * 
 * TODO: this class probably needs to delegate to a strategy object pulled out
 * of the appcontext, to provide ability to override and configre. Crufty, but
 * JSF provides no other way to customize and configure this insance
 * 
 * @author Colin Sampaleanu
 * @since 1.0
 */
public class FlowPropertyResolver extends PropertyResolver {

	/**
	 * Logger.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Default property resolver.
	 */
	protected final PropertyResolver originalPropertyResolver;

	/**
	 * Create a new PropertyResolver, using the given original PropertyResolver.
	 * <p>
	 * A JSF implementation will automatically pass its original resolver into
	 * the constructor of a configured resolver, provided that there is a
	 * corresponding constructor argument.
	 * 
	 * @param originalPropertyResolver the original VariableResolver
	 */
	public FlowPropertyResolver(PropertyResolver originalPropertyResolver) {
		this.originalPropertyResolver = originalPropertyResolver;
	}

	public Class getType(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.getType(base, index);
		}
		else {
			return null;
		}
	}

	public Class getType(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.getType(base, property);
		}
		if (property == null) {
			throw new PropertyNotFoundException("Unable to get value from Flow, as property (key) is null");
		}
		if (!(property instanceof String)) {
			throw new PropertyNotFoundException("Unable to get value from Flow map, as key is non-String");
		}
		FlowExecution execution = (FlowExecution)base;
		// we want to access flow scope of the active session (conversation)
		Object value = execution.getActiveSession().getScope().getAttribute((String)property);
		// note that MyFaces returns Object.class for a null value here, but as
		// I read the JSF spec, null should be returned when the object type can
		// not be determined this certainly seems to be the case for a map value
		// which doesn' even exist
		return (value == null) ? null : value.getClass();
	}

	public Object getValue(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.getValue(base, index);
		} else {
			throw new ReferenceSyntaxException("Cannot apply an index value to Flow map");
		}
	}

	public Object getValue(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.getValue(base, property);
		}
		if (!(property instanceof String)) {
			throw new PropertyNotFoundException("Unable to get value from Flow map, as key is non-String");
		}
		FlowExecution execution = (FlowExecution)base;
		String attributeName = (String)property;
		Object value = execution.getActiveSession().getScope().getAttribute(attributeName);
		if (value == null) {
			FacesContext fc = FacesContext.getCurrentInstance();
			Assert.notNull(fc, "FacesContext must exist during property resolution stage");
			WebApplicationContext wac = getWebApplicationContext(fc);
			if (wac.containsBean(attributeName)) {
				// note: this resolver doesn't care, but this should normally be
				// either a stateless singleton bean, or a stateful/stateless
				// prototype
				value = wac.getBean(attributeName);
				execution.getActiveSession().getScope().setAttribute(attributeName, value);
			}
		}
		return value;
	}

	public boolean isReadOnly(Object base, int index) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.isReadOnly(base, index);
		}
		else {
			return false;
		}
	}

	public boolean isReadOnly(Object base, Object property) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			return originalPropertyResolver.isReadOnly(base, property);
		}
		else {
			return false;
		}
	}

	public void setValue(Object base, int index, Object value) throws EvaluationException, PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			originalPropertyResolver.setValue(base, index, value);
		}
		else {
			throw new ReferenceSyntaxException("Can not apply an index value to Flow map");
		}
	}

	public void setValue(Object base, Object property, Object value) throws EvaluationException,
			PropertyNotFoundException {
		if (!(base instanceof FlowExecution)) {
			originalPropertyResolver.setValue(base, property, value);
			return;
		}
		if (property == null || !(property instanceof String)
				|| (property instanceof String && ((String)property).length() == 0)) {
			throw new PropertyNotFoundException(
					"Attempt to set Flow attribute with null name, empty name, or non-String name");
		}
		FlowExecution execution = (FlowExecution)base;
		execution.getActiveSession().getScope().setAttribute((String)property, value);
	}

	/**
	 * Retrieve the web application context to delegate bean name resolution to.
	 * <p>
	 * Default implementation delegates to FacesContextUtils.
	 * 
	 * @param facesContext the current JSF context
	 * @return the Spring web application context (never <code>null</code>)
	 * @see FacesContextUtils#getRequiredWebApplicationContext
	 */
	protected WebApplicationContext getWebApplicationContext(FacesContext facesContext) {
		return FacesContextUtils.getRequiredWebApplicationContext(facesContext);
	}
}