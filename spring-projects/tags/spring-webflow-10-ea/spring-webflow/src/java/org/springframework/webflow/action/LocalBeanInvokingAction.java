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

import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to a method on an arbitrary bean. The bean
 * instance is managed locally by this Action in an instance variable.
 * 
 * @author Keith Donald
 */
public class LocalBeanInvokingAction extends AbstractBeanInvokingAction {

	/**
	 * The target bean (any POJO) to invoke.
	 */
	private Object targetBean;

	/**
	 * Creates a bean invoking action that invokes the specified bean. The bean
	 * may be a proxy.
	 * @param targetBean the bean to wrap
	 */
	public LocalBeanInvokingAction(Object targetBean) {
		setTargetBean(targetBean);
	}

	/**
	 * Returns the target bean.
	 */
	public Object getTargetBean() {
		return targetBean;
	}

	/**
	 * Set the target bean to wrap.
	 */
	public void setTargetBean(Object targetBean) {
		Assert.notNull(targetBean, "The target bean for this action to invoke cannot be null");
		this.targetBean = targetBean;
	}

	protected Object getBean(RequestContext context) {
		return getTargetBean();
	}
}