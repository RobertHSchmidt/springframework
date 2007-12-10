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

package org.springframework.config.java.annotation;

/**
 * Signifies {@link Bean}s as primary for disambiguation when looking up beans
 * by type.
 * 
 * @see Bean#primary()
 * @see org.springframework.beans.factory.TypeSafeBeanFactory#getBean(Class)
 * 
 * @author Chris Beams
 */
public enum Primary {

	UNSPECIFIED(-1), FALSE(0), TRUE(1);

	private final int value;

	Primary(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public boolean booleanValue() {
		return this == TRUE;
	}

}
