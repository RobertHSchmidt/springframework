/**
 * 
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