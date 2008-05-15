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
package org.springframework.config.java.core;

/**
 * @author Chris Beams
 */
public final class Constants {

	private Constants() {
	}

	public static final String JAVA_CONFIG_PKG = "org.springframework.config.java";

	/**
	 * Used as metadata on framework-internal bean definitions that should not
	 * count when tallying bean def registration counts during testing
	 */
	public static final String JAVA_CONFIG_IGNORE = "JAVA_CONFIG_IGNORE";

	/**
	 * Name of internal bean factory used for reducing the visibility of 'hidden' beans
	 */
	public static final String INTERNAL_BEAN_FACTORY_NAME = "SJC_INTERNAL_BF";

}
