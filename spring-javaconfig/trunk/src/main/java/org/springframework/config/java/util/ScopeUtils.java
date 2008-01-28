/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java.util;

import org.springframework.util.Assert;

/**
 * Utility class related to scoped bean and scoped proxies.
 * 
 * <p/> Note: This class is used internally by the framework, it is not intended
 * to be used by applications.
 * 
 * @see org.springframework.config.java.annotation.aop.ScopedProxy
 * @see org.springframework.config.java.process.ScopedProxyConfigurationListener
 * @author Costin Leau
 * 
 */
public abstract class ScopeUtils {

	private static final String TARGET_NAME_PREFIX = "scopedTarget.";

	/**
	 * Return the <i>hidden</i> name based on a scoped proxy bean name.
	 * 
	 * @param scopedProxy the scope proxy bean name
	 * @return the scope proxy bean 'hidden' name
	 */
	public static String getScopedHiddenName(String scopedProxy) {
		Assert.hasText(scopedProxy);
		return TARGET_NAME_PREFIX.concat(scopedProxy);
	}

	public static boolean isHiddenScopedBean(String beanName) {
		Assert.hasText(beanName);
		return beanName.startsWith(TARGET_NAME_PREFIX);
	}

}
