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
package org.springframework.webflow.context.support;

import org.springframework.webflow.context.ExternalContext;

/**
 * Simple holder class that associates a {@link ExternalContext} instance with
 * the current thread. The ExternalContext will be inherited by any child
 * threads spawned by the current thread.
 * <p>
 * Used as a central holder for the current ExternalContext in Spring Web Flow,
 * wherever necessary. Often used by artifacts needing access to the current
 * application session.
 * 
 * @author Keith Donald
 * @see ExternalContext
 */
public final class ExternalContextHolder {

	private static ThreadLocal externalContextHolder = new InheritableThreadLocal();

	/**
	 * Associate the given LocaleContext with the current thread.
	 * @param externalContext the current LocaleContext, or <code>null</code>
	 * to reset the thread-bound context
	 */
	public static void setExternalContext(ExternalContext externalContext) {
		externalContextHolder.set(externalContext);
	}

	/**
	 * Return the LocaleContext associated with the current thread, if any.
	 * @return the current LocaleContext, or <code>null</code> if none
	 */
	public static ExternalContext getExternalContext() {
		return (ExternalContext)externalContextHolder.get();
	}

	// not instantiable
	private ExternalContextHolder() {

	}
}