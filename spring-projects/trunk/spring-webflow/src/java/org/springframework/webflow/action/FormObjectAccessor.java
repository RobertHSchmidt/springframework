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
package org.springframework.webflow.action;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Convenience helper that encapsulates logic on how to retrieve and expose
 * form objects and associated errors to and from a flow execution context.
 * <p>
 * <b>Note</b>: The form object available under the well known attribute name
 * {@link #FORM_OBJECT_ALIAS} will be the last form object set in the
 * request context. The same is true for the associated errors object. This
 * implies that special care should be taken when accessing the form object
 * using this alias if there are multiple form objects available in the
 * flow execution context!
 * 
 * @see org.springframework.webflow.RequestContext
 * @see org.springframework.validation.Errors
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FormObjectAccessor {

	/**
	 * The form object instance is aliased under this attribute name in the
	 * flow context by the default form setup and bind and validate actions.
	 * <p>
	 * Note that if you would have multiple form objects in the request context,
	 * the last one that was used would be available using this alias!
	 */
	public static final String FORM_OBJECT_ALIAS = "#formObject";
	
	private RequestContext context;

	/**
	 * Creates a form object accessor that wraps the given context.
	 * @param context the flow execution request context
	 */
	public FormObjectAccessor(RequestContext context) {
		this.context = context;
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ALIAS}. Will try all scopes.
	 * @return the form object, or null if not found
	 */
	public Object getFormObject() {
		Object formObject = getFormObject(ScopeType.REQUEST);
		if (formObject != null) {
			return formObject;
		}
		formObject = getFormObject(ScopeType.FLOW);
		if (formObject != null) {
			return formObject;
		}
		return getFormObject(ScopeType.CONVERSATION);
	}

	/**
	 * Gets the form object from the context, using the well-known attribute
	 * name {@link #FORM_OBJECT_ALIAS}.
	 * @param scope the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getFormObject(ScopeType scope) {
		return getFormObject(FORM_OBJECT_ALIAS, scope);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form object in the context
	 * @param scopeType the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getFormObject(String formObjectName, ScopeType scopeType) {
		return scopeType.getScope(context).getAttribute(formObjectName);
	}

	/**
	 * Gets the form object from the context, using the specified name.
	 * @param formObjectName the name of the form in the context
	 * @param formObjectClass the class of the form object, which will be verified
	 * @param scopeType the scope to obtain the form object from
	 * @return the form object, or null if not found
	 */
	public Object getFormObject(String formObjectName, Class formObjectClass, ScopeType scopeType) {
		return scopeType.getScope(context).getAttribute(formObjectName, formObjectClass);
	}

	/**
	 * Expose given form object using given name in specified scope.
	 * @param formObject the form object
	 * @param formObjectName the name of the form object
	 * @param scopeType the scope in which to expose the form object
	 */
	public void setFormObject(Object formObject, String formObjectName, ScopeType scopeType) {
		scopeType.getScope(context).setAttribute(formObjectName, formObject);
		alias(formObject, scopeType);
	}

	/**
	 * Expose given form object using the well known alias
	 * {@link #FORM_OBJECT_ALIAS} in the specified scope.
	 * @param formObject the form object
	 * @param scopeType the scope in which to expose the form object
	 */
	private void alias(Object formObject, ScopeType scopeType) {
		scopeType.getScope(context).setAttribute(FORM_OBJECT_ALIAS, formObject);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #FORM_OBJECT_ALIAS}.
	 * This method will search all scopes.
	 * @return the form object Errors tracker, or null if not found
	 */
	public Errors getFormErrors() {
		Errors errors = getFormErrors(ScopeType.REQUEST);
		if (errors != null) {
			return errors;
		}
		errors = getFormErrors(ScopeType.FLOW);
		if (errors != null) {
			return errors;
		}
		return getFormErrors(ScopeType.CONVERSATION);

	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the form object name {@link #FORM_OBJECT_ALIAS}.
	 * @param scopeType the scope to obtain the errors from
	 * @return the form object Errors tracker, or null if not found
	 */
	public Errors getFormErrors(ScopeType scopeType) {
		return getFormErrors(FORM_OBJECT_ALIAS, scopeType);
	}

	/**
	 * Gets the form object <code>Errors</code> tracker from the context,
	 * using the specified form object name.
	 * @param formObjectName the name of the Errors object, which will be
	 *        prefixed with {@link BindException#ERROR_KEY_PREFIX}
	 * @param scopeType the scope to obtain the errors from
	 * @return the form object errors instance, or null if not found
	 */
	public Errors getFormErrors(String formObjectName, ScopeType scopeType) {
		return (Errors)scopeType.getScope(context).getAttribute(BindException.ERROR_KEY_PREFIX + formObjectName,
				Errors.class);
	}

	/**
	 * Expose given errors instance in the specified scope.
	 * @param errors the errors object
	 * @param scopeType the scope to expose the errors in
	 */
	public void setFormErrors(Errors errors, ScopeType scopeType) {
		scopeType.getScope(context).setAttribute(BindException.ERROR_KEY_PREFIX + errors.getObjectName(), errors);
		alias(errors, scopeType);
	}
	
	/**
	 * Expose given errors instance using the well known alias
	 * {@link #FORM_OBJECT_ALIAS} in the specified scope.
	 * @param errors the errors instance
	 * @param scopeType the scope in which to expose the errors instance
	 */
	private void alias(Errors errors, ScopeType scopeType) {
		scopeType.getScope(context).setAttribute(BindException.ERROR_KEY_PREFIX + FORM_OBJECT_ALIAS, errors);
	}
}