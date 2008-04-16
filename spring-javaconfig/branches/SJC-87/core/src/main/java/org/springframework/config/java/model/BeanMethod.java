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
package org.springframework.config.java.model;

import static java.lang.String.format;

import org.springframework.config.java.annotation.Bean;

public class BeanMethod {
	private Bean beanAnnotation;
	private final String methodName;

	public BeanMethod(String methodName) {
		this.methodName = methodName;
	}

	public Bean getBeanAnnotation() {
		return beanAnnotation;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public String toString() {
		return format("{%s:methodName=%s}", getClass().getSimpleName(), methodName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeanMethod other = (BeanMethod) obj;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		}
		else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}

}