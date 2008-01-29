/*
 * Copyright 2002-2008 the original author or authors.
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

/**
 * Mechanisms commonly used when ensuring minimal external dependencies
 * throughout the codebase.
 * 
 * TODO: delete in favor of using ClassUtils.isPresent()
 * 
 * @author cbeams
 */
// see TODO above
@Deprecated
public final class DependencyUtils {

	private DependencyUtils() {
	}

	private static boolean isAopAvailable;

	static {
		try {
			Class.forName("org.springframework.aop.TargetSource");
			isAopAvailable = true;
		}
		catch (ClassNotFoundException ex) {
			isAopAvailable = false;
		}
	}

	/**
	 * Inspects the classpath to determine whether key AOP resources are
	 * present.
	 */
	public static boolean isAopAvailable() {
		return isAopAvailable;
	}
}
