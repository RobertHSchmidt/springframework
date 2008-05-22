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
package org.springframework.config.java.naming;

import java.lang.reflect.Method;

import org.springframework.config.java.model.ConfigurationClass;
import org.springframework.config.java.model.ModelMethod;
import org.springframework.util.Assert;

/**
 * Naming strategy which uses the method name for generating the bean name.
 * Allows configuration for include the owning class short name or FQN as well.
 *
 * @author Costin Leau
 */
public class MethodNameStrategy implements BeanNamingStrategy {

	/**
	 * Naming prefix.
	 *
	 * @author Costin Leau
	 *
	 */
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

	/**
	 * Default, empty constructor.
	 */
	public MethodNameStrategy() {
	}

	/**
	 * Constructor allowing the naming prefix to be specified.
	 * @param prefix naming prefix
	 */
	public MethodNameStrategy(Prefix prefix) {
		this.prefix = prefix;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.naming.BeanNamingStrategy#getBeanName(java.lang.reflect.Method,
	 * org.springframework.config.java.annotation.Bean,
	 * org.springframework.config.java.annotation.Configuration)
	 */
	public String getBeanName(Method beanCreationMethod) {
		throw new UnsupportedOperationException();
	}

	public String getBeanName(ModelMethod modelMethod) {
		Assert.notNull(modelMethod, "modelMethod is required");

		String beanName = modelMethod.getName();
		ConfigurationClass declaringClass = modelMethod.getDeclaringClass();
		Assert.notNull(declaringClass, "declaringClass was not specified for " + modelMethod);

		switch (prefix) {
    		case CLASS:
    			beanName = declaringClass.getName().concat(".").concat(beanName);
    			break;

    		case FQN:
    			beanName = declaringClass.getFullyQualifiedName().concat(".").concat(beanName);
    			break;

    		default:
    			// no-op
    			break;
		}

		return beanName;
	}

	public void setPrefix(Prefix prefix) {
		this.prefix = prefix;
	}



}
