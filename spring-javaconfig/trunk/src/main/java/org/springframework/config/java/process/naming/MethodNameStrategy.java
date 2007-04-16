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
package org.springframework.config.java.process.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Naming strategy which uses the method name for geneting the bean name. Allows
 * configuration for include the owning class short name or FQN as well.
 * 
 * @author Costin Leau
 * 
 */
public class MethodNameStrategy implements BeanNamingStrategy {

	public enum Prefix {

		/**
		 * No prefix is used (i.e. someMethod)
		 */
		NONE(0),

		/**
		 * The declaring class short name is used as prefix (i.e.
		 * SomeClass.someMethod)
		 */
		CLASS(1),

		/**
		 * The FQN of the method will be used (i.e.
		 * my.package.SomeClass.someMethod)
		 */
		FQN(2);

		private final int value;

		Prefix(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	private Prefix prefix = Prefix.NONE;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.process.naming.BeanNamingStrategy#getBeanName(java.lang.reflect.Method,
	 * org.springframework.config.java.annotation.Bean,
	 * org.springframework.config.java.annotation.Configuration)
	 */
	public String getBeanName(Method beanCreationMethod, Configuration configuration) {
		Assert.notNull(beanCreationMethod, "beanCreationMethod is required");
		StringBuilder builder = new StringBuilder();

		switch (prefix) {
		case CLASS:
			builder.append(beanCreationMethod.getDeclaringClass().getSimpleName());
			builder.append(".");
			break;

		case FQN:
			builder.append(beanCreationMethod.getDeclaringClass().getName());
			builder.append(".");
			break;

		default:
			// no-op
			break;
		}
		builder.append(beanCreationMethod.getName());
		return builder.toString();
	}

	public void setPrefix(Prefix prefix) {
		this.prefix = prefix;
	}

}
