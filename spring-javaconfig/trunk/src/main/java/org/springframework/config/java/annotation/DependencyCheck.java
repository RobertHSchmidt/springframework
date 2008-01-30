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
package org.springframework.config.java.annotation;

import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * Annotation representing a Spring IoC dependency check status.
 * 
 * @author Rod Johnson
 * @see AbstractBeanDefinition
 */
public enum DependencyCheck {

	UNSPECIFIED(-1), NONE(AbstractBeanDefinition.DEPENDENCY_CHECK_NONE), SIMPLE(
			AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE), OBJECTS(AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS), ALL(
			AbstractBeanDefinition.DEPENDENCY_CHECK_ALL);

	private final int value;

	DependencyCheck(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

}
