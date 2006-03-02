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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;
import org.springframework.webflow.ViewSelection;

/**
 * Concerete response type that requests an redirect to an <i>existing</i>,
 * active Spring Web Flow conversation at a SWF-specific <i>conversation URL</i>.
 * This enables the triggering of redirect after post semantics from within an
 * <i>active</i> flow execution.
 * <p>
 * Once the redirect response is issued, the configured
 * {@link #getApplicationView()} is treated as the view to render on the
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
	private final ApplicationView applicationView;

	/**
	 * Creates a new conversation redirect.
	 * @param applicationView the view to render on receipt of the conversation
	 * redirect request.
	 */
	public ConversationRedirect(ApplicationView applicationView) {
		this.applicationView = applicationView;
	}

	/**
	 * Return the application view to render on receipt of the conversation
	 * redirect request.
	 */
	public ApplicationView getApplicationView() {
		return applicationView;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ConversationRedirect)) {
			return false;
		}
		ConversationRedirect other = (ConversationRedirect)o;
		return ObjectUtils.nullSafeEquals(applicationView, other.applicationView);
	}

	public int hashCode() {
		return applicationView.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("applicationView", applicationView).toString();
	}
}