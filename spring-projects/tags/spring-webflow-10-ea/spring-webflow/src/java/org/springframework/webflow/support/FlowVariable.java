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
package org.springframework.webflow.support;

import java.io.Serializable;

import org.springframework.beans.BeanUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * A value object that defines a specification for a flow variable. Encapsulates
 * information about the variable (name and type), and the behavior necessary
 * to create a new variable instance in a flow execution scope.
 * @author Keith Donald
 */
public class FlowVariable implements Serializable {

	/**
	 * The variable name.
	 */
	private String name;

	/**
	 * The variable type.
	 */
	private Class type;

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 * @param type the variable type
	 */
	public FlowVariable(String name, Class type) {
		Assert.notNull(name, "The variable name is required");
		Assert.notNull(type, "The variable type is required");
		this.name = name;
		this.type = type;
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowVariable)) {
			return false;
		}
		FlowVariable other = (FlowVariable)o;
		return name.equals(other.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Creates a new instance of this flow variable in flow scope.
	 * @param context the flow execution request context
	 */
	public void create(RequestContext context) {
		context.getFlowScope().put(name, BeanUtils.instantiateClass(type));
	}

	public String toString() {
		return new ToStringCreator(this).append("name", name).append("type", type).toString();
	}
}