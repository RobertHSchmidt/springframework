package org.springframework.webflow.action;

import org.springframework.webflow.RequestContext;

/**
 * A service for managing the saving and restoring of state associated with a
 * invokable bean. State is save/restored from flow scope.
 * @author Keith Donald
 */
public interface BeanStatePersister {

	/**
	 * Save the beans state out to flow scope.
	 * @param bean the bean
	 * @param context the flow execution request context
	 * @throws Exception an exception occured
	 */
	public void saveState(Object bean, RequestContext context) throws Exception;

	/**
	 * Restore the bean's state from flow scope.
	 * @param bean the bean
	 * @param context the flow execution request context
	 * @throws Exception an exception occured
	 */
	public void restoreState(Object bean, RequestContext context) throws Exception;

}
