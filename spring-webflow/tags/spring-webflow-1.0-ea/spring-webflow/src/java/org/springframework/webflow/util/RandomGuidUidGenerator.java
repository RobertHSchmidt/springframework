/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.util;

import java.io.Serializable;

/**
 * A key generator that uses the RandomGuid support class. The default
 * implementation used by the webflow system.
 * 
 * @author Keith Donald
 */
public class RandomGuidUidGenerator implements UidGenerator, Serializable {

	/**
	 * Should the random GUID generated be secure?
	 */
	private boolean secure;

	/**
	 * Returns the secure flag.
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * Sets the secure flag.
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public Serializable generateId() {
		return new RandomGuid(secure).toString();
	}
}