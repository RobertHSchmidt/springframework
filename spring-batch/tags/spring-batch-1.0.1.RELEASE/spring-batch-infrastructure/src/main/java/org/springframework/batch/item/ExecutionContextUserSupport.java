/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.item;

import org.springframework.util.Assert;

/**
 * Facilitates assigning names to objects persisting data in
 * {@link ExecutionContext} and generating keys for {@link ExecutionContext}
 * based on the name.
 * 
 * @author Robert Kasanicky
 */
public class ExecutionContextUserSupport {

	private String name;

	/**
	 * @param name unique name for the item stream used to create execution
	 * context keys.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Prefix the argument with the name of the item stream to create a unique
	 * key that can be safely used to identify data stored in
	 * {@link ExecutionContext}.
	 */
	protected String getKey(String s) {
		Assert.hasLength(name, "ItemStream must have a name assigned.");
		return name + "." + s;
	}

}
