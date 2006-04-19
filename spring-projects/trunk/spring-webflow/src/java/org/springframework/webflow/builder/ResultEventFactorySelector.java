/**
 * 
 */
package org.springframework.webflow.builder;

import org.springframework.binding.method.ClassMethodKey;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.action.ResultEventFactory;
import org.springframework.webflow.action.ResultObjectBasedEventFactory;
import org.springframework.webflow.action.SuccessEventFactory;

public class ResultEventFactorySelector {
	private SuccessEventFactory successEventFactory = new SuccessEventFactory();

	private ResultObjectBasedEventFactory resultObjectBasedEventFactory = new ResultObjectBasedEventFactory();

	public ResultEventFactory forMethod(MethodSignature signature, Class beanClass) {
		ClassMethodKey key = new ClassMethodKey(beanClass, signature.getMethodName(), signature.getParameters()
				.getTypesArray());
		if (resultObjectBasedEventFactory.isMappedValueType(key.getMethod().getReturnType())) {
			return resultObjectBasedEventFactory;
		}
		else {
			return successEventFactory;
		}
	}
}