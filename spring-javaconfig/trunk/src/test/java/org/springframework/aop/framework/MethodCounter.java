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

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Useful abstract superclass for counting advices etc.
 * 
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class MethodCounter implements Serializable {

	/** Method name --> count, does not understand overloading */
	private HashMap<String, Integer> map = new HashMap<String, Integer>();

	private int allCount;

	protected void count(Method m) {
		count(m.getName());
	}

	protected void count(String methodName) {
		Integer I = map.get(methodName);
		I = (I != null) ? new Integer(I.intValue() + 1) : new Integer(1);
		map.put(methodName, I);
		++allCount;
	}

	public int getCalls(String methodName) {
		Integer I = map.get(methodName);
		return (I != null) ? I.intValue() : 0;
	}

	public int getCalls() {
		return allCount;
	}

	/**
	 * A bit simplistic: just wants the same class. Doesn't worry about counts.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || other.getClass() != this.getClass()) {
			return false;
		}

		// TODO: not sure if this cast is part of the test
		@SuppressWarnings("unused")
		MethodCounter mc2 = (MethodCounter) other;
		return true;
	}

}
