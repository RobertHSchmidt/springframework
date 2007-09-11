/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.builder.xml;

import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecutionStatus;

public class TestPojo {
	private boolean flag;

	public boolean booleanMethod() {
		return true;
	}

	public FlowExecutionStatus enumMethod() {
		return FlowExecutionStatus.CREATED;
	}

	public void methodWithVariableArgument(FlowExecutionStatus status) {
		Assert.isTrue(status == FlowExecutionStatus.CREATED);
	}

	public void methodWithConstantArgument(String constant) {
		Assert.isTrue(constant.equals("A constant"));
	}

	public void methodWithArgumentTypeConversion(FlowExecutionStatus status) {
		Assert.isTrue(status == FlowExecutionStatus.CREATED);
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
}