/*
 * Copyright 2002-2006 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Base action implementation that provides a number of helper methods generally
 * useful to any action command. These include:
 * <ul>
 * <li>Creating common action result events
 * <li>Inserting action pre and post execution logic
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractAction extends EventFactorySupport implements Action, InitializingBean {

	/**
	 * Logger, usable in subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	public void afterPropertiesSet() throws Exception {
		try {
			initAction();
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Initialization of Action failed: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Action initializing callback, may be overriden by subclasses to perform
	 * custom initialization logic.
	 */
	protected void initAction() throws Exception {
	}

	public final Event execute(RequestContext context) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Action '" + getClass().getName() + "' beginning execution");
		}
		Event result = doPreExecute(context);
		if (result == null) {
			result = doExecute(context);
			if (logger.isDebugEnabled()) {
				if (result != null) {
					logger.debug("Action '" + getClass().getName() + "' completed execution; result is '"
							+ result.getId() + "'");
				}
				else {
					logger.debug("Action '" + getClass().getName() + "' completed execution; "
							+ "returned result is [null]");
				}
			}
			doPostExecute(context);
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("Action execution disallowed; pre-execute result is '" + result.getId() + "'");
			}
		}
		return result;
	}

	/**
	 * Pre-action-execution hook, subclasses may override. If this method
	 * returns a non-<code>null</code> event, the <code>doExecute()</code>
	 * method will <b>not</b> be called and the returned event will be used to
	 * select a transition to trigger in the calling action state. If this
	 * method returns <code>null</code>, <code>doExecute()</code> will be
	 * called to obtain an action result event.
	 * <p>
	 * This implementation just returns <code>null</code>.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return the non-<code>null</code> action result, in which case the
	 * <code>doExecute()</code> will not be called, or <code>null</code> if
	 * the <code>doExecute()</code> method should be called to obtain the
	 * action result
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	protected Event doPreExecute(RequestContext context) throws Exception {
		return null;
	}

	/**
	 * Template hook method subclasses should override to encapsulate their
	 * specific action execution logic.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @return the action result event
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	protected abstract Event doExecute(RequestContext context) throws Exception;

	/**
	 * Post-action execution hook, subclasses may override.
	 * <p>
	 * This implementation does nothing.
	 * @param context the action execution context, for accessing and setting
	 * data in "flow scope" or "request scope"
	 * @throws Exception an <b>unrecoverable</b> exception occured, either
	 * checked or unchecked
	 */
	protected void doPostExecute(RequestContext context) throws Exception {
	}
}