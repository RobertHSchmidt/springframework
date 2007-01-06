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
package org.springframework.osgi.service.support.cardinality;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.springframework.aop.TargetSource;
import org.springframework.osgi.service.support.RetryTemplate;
import org.springframework.osgi.service.support.ServiceWrapper;

/**
 * Base class for OSGi cardinality target sources.
 * 
 * @author Costin Leau
 * 
 */
public abstract class OsgiServiceTargetSource implements TargetSource {

	private final Log logger = LogFactory.getLog(getClass());

	protected RetryTemplate retryTemplate = new RetryTemplate();

	protected BundleContext context;

	protected String clazz;

	protected String filter;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public Class getTargetClass() {
		// TODO : change current implementation
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.TargetSource#isStatic()
	 */
	public boolean isStatic() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object target) throws Exception {
		// default empty implementation
	}

	public void setRetryTemplate(RetryTemplate retryTemplate) {
		this.retryTemplate = retryTemplate;
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	public void setClass(String clazz) {
		this.clazz = clazz;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

}
