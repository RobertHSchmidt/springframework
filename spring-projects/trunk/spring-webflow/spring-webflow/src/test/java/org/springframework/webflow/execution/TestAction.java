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
package org.springframework.webflow.execution;

import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;

public class TestAction extends AbstractAction {
	private Event result = new Event(this, "success");

	private boolean executed;

	private int executionCount;

	public TestAction() {

	}

	public TestAction(String result) {
		if (StringUtils.hasText(result)) {
			this.result = new Event(this, result);
		}
		else {
			this.result = null;
		}
	}

	public boolean isExecuted() {
		return executed;
	}

	public int getExecutionCount() {
		return executionCount;
	}

	protected Event doExecute(RequestContext context) throws Exception {
		executed = true;
		executionCount++;
		return result;
	}
}