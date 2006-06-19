package org.springframework.webflow.action;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Interface defining core action methods for working with input forms.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FormActionMethods {

	/**
	 * Prepares a form object for display in a new form, loading it if necessary.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" when binding and validation is successful
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event setupForm(RequestContext context) throws Exception;

	/**
	 * Bind incoming request parameters to allowed fields of the form object and
	 * then validate the bound form object if a validator is configured.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" when binding and validation is successful, "error" if
	 * ther were binding and/or validation errors
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event bindAndValidate(RequestContext context) throws Exception;

	/**
	 * Bind incoming request parameters to allowed fields of the form object.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" if there are no binding errors, "error" otherwise
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event bind(RequestContext context) throws Exception;

	/**
	 * Validate the form object.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" if there are no validation errors, "error" otherwise
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event validate(RequestContext context) throws Exception;

	/**
	 * Resets the form by clearing out the form object in the specified scope
	 * and reloading it.
	 * @param context the request context
	 * @return "success" if the reset action completed successfully
	 * @throws Exception if an exception occured
	 */
	public Event resetForm(RequestContext context) throws Exception;
}