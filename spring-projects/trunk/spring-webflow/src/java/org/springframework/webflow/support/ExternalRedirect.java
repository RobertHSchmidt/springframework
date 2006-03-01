/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.util.ObjectUtils;
import org.springframework.webflow.ViewSelection;

/**
 * Requests redirect to an external URL outside of Spring Web Flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class ExternalRedirect extends ViewSelection {

	/**
	 * The url path to redirect to.
	 */
	private final String url;

	/**
	 * Creates a external redirect request.
	 * @param url the url path to redirect to
	 */
	public ExternalRedirect(String url) {
		this.url = url;
	}

	/**
	 * Returns the external URL to redirect to.
	 */
	public String getUrl() {
		return url;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ExternalRedirect)) {
			return false;
		}
		ExternalRedirect other = (ExternalRedirect)o;
		return ObjectUtils.nullSafeEquals(url, other.url);
	}

	public int hashCode() {
		return url.hashCode();
	}
}