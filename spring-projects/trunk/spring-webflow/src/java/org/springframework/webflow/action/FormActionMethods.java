package org.springframework.webflow.action;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Interface defining core action methods for working with input forms.
 * 
 * @author Keith Donald
 */
public interface FormActionMethods {

	/**
	 * Loads the form object and ensures it is exposed in the model of the
	 * executing flow in the correct scope.
	 * <p>
	 * This is a fine-grained action method that you may invoke and combine with
	 * other action methods as part of a chain. For example, one could call
	 * "exposeFormObject" and then "bind" to achieve setupForm-like behaviour,
	 * with the ability to respond to results of each actions independently as
	 * part of a flow definition.
	 * <p>
	 * Here is that example of that 'action chaining' illustrated:
	 * 
	 * <pre>
	 *  &lt;action-state method=&quot;setupForm&quot;&gt;
	 *      &lt;action name=&quot;exposer&quot; bean=&quot;formAction&quot; method=&quot;exposeFormObject&quot;/&gt;
	 *      &lt;action bean=&quot;formAction&quot; method=&quot;bind&quot;/&gt;
	 *      &lt;transition on=&quot;exposer.error&quot; to=&quot;displayFormObjectRetrievalFailurePage&quot;/&gt;
	 *      &lt;transition on=&quot;success&quot; to=&quot;displayForm&quot;/&gt;
	 *  &lt;/action-state&gt;
	 * </pre>
	 * 
	 * @param context the flow request context
	 * @return "success" if the action completed successsfully, "error"
	 * otherwise
	 * @throws Exception an unrecoverable exception occured
	 */
	public Event exposeFormObject(RequestContext context) throws Exception;

	/**
	 * Prepares a form object for display in a new form. This will initialize
	 * the binder so that all custom property editors are available for use in
	 * the new form.
	 * <p>
	 * If the setupBindingEnabled method returns true a data binding operation
	 * will occur to pre-populate the new form with incoming event parameters.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" when binding and validation is successful, "error" if
	 * there were binding or validation errors or the form object could not be
	 * retrieved
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
	 * ther were errors or the form object could not be retrieved
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event bindAndValidate(RequestContext context) throws Exception;

	/**
	 * Bind incoming request parameters to allowed fields of the form object.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" if there are no binding errors, "error" if there are
	 * errors or the form object could not be retrieved
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event bind(RequestContext context) throws Exception;

	/**
	 * Validate the form object.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return "success" if there are no validation errors, "error" if there are
	 * errors or the form object could not be retrieved
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	public Event validate(RequestContext context) throws Exception;

	/**
	 * Resets the form by clearing out the form object in the specified scope
	 * and reloading it.
	 * @param context the request context
	 * @return "success" if the reset action completed successfully, "error"
	 * otherwise
	 * @throws Exception if an exception occured
	 */
	public Event resetForm(RequestContext context) throws Exception;
}