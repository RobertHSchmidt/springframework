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
package org.springframework.webflow;

import java.io.Serializable;

/**
 * Base class for value objects that provide clients with information about a
 * logical response to issue and the dynamic application "model" data necessary
 * to render it.
 * <p>
 * View selections are returned as a result of entering a {@link ViewState} or
 * {@link EndState}, typically created by those states delegating to a
 * {@link ViewSelector} factory. When a state of either of those types is
 * entered and returns, the caller into the web flow system is handed a
 * fully-configured <code>ViewSelection</code> instance and is expected to
 * present a screen to the user that allows them to interact at that point
 * within the flow.
 * 
 * @see org.springframework.webflow.ViewSelector
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class ViewSelection implements Serializable {

	/**
	 * Serialization version uid.
	 */
	private static final long serialVersionUID = -7048182063951237313L;

	/**
	 * Constant for a <code>null</code> or empty view selection, indicating no
	 * response should be issued.
	 */
	public static final ViewSelection NULL_VIEW_SELECTION = new NullViewSelection();

	private static class NullViewSelection extends ViewSelection {
	}
}