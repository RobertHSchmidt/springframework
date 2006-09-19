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
package org.springframework.webflow.engine.builder.xml;

import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowSessionStatus;

public class TestPojo {
	public boolean booleanMethod() {
		return true;
	}

	public FlowSessionStatus enumMethod() {
		return FlowSessionStatus.CREATED;
	}
	
	public void methodWithVariableArgument(FlowSessionStatus status) {
		Assert.isTrue(status == FlowSessionStatus.CREATED);
	}

	public void methodWithConstantArgument(String constant) {
		Assert.isTrue(constant.equals("A constant"));
	}

	public void methodWithArgumentTypeConversion(FlowSessionStatus status) {
		Assert.isTrue(status == FlowSessionStatus.CREATED);
	}
}