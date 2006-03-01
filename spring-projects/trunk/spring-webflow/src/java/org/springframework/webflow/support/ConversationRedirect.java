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
 * Requests an redirect to an <i>existing</i> conversation at a SWF-specific
 * <i>conversation URL</i>. This enables redirect after post semantics from
 * within an <i>active</i> flow execution.
 * <p>
 * Once the redirect response is issued, the configured
 * {@link #getViewSelection()} is treated as the view to forward on the
 * subsequent request issued from the browser, targeted at the conversation URL.
 * The conversation URL is stabally refreshable (and bookmarkable) while the
 * conversation remains active.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class ConversationRedirect extends ViewSelection {

	/**
	 * The view to render to on receipt of subsequent conversation requests.
	 */
	private final ApplicationViewSelection viewSelection;

	/**
	 * Creates a new conversation redirect.
	 * @param viewSelection the view to render on receipt of the conversation
	 * redirect request.
	 */
	public ConversationRedirect(ApplicationViewSelection viewSelection) {
		this.viewSelection = viewSelection;
	}

	/**
	 * Return the application view to render on receipt of the conversation
	 * redirect request.
	 */
	public ApplicationViewSelection getViewSelection() {
		return viewSelection;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConversationRedirect)) {
			return false;
		}
		ConversationRedirect other = (ConversationRedirect)o;
		return ObjectUtils.nullSafeEquals(viewSelection, other.viewSelection);
	}

	public int hashCode() {
		return viewSelection.hashCode();
	}
}