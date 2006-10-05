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
package org.springframework.binding.method;

import java.io.Serializable;

import org.springframework.binding.expression.Expression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * A named method parameter. Each parameter has an identifying name and is of a
 * specified type (class).
 * 
 * @author Keith
 */
public class Parameter implements Serializable {

	/**
	 * The class of the parameter, e.g "springbank.AccountNumber".
	 */
	private Class type;

	/**
	 * The name of the parameter as an evaluatable expression, e.g
	 * "accountNumber".
	 */
	private Expression name;

	/**
	 * Create a new named parameter definition.
	 * 
	 * @param type the type
	 * @param name the name
	 */
	public Parameter(Class type, Expression name) {
		Assert.notNull(name, "The parameter name expression is required");
		this.type = type;
		this.name = name;
	}

	public Class getType() {
		return type;
	}

	public Expression getName() {
		return name;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Parameter)) {
			return false;
		}
		Parameter other = (Parameter)obj;
		return ObjectUtils.nullSafeEquals(type, other.type) && name.equals(other.name);
	}

	public int hashCode() {
		return (type != null ? type.hashCode() : 0) + name.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("type", type).append("name", name).toString();
	}
}