package org.springframework.config.java.enhancement;

import java.lang.reflect.Method;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;

public interface BeanMethodReturnValueProcessor {

	boolean processBeanMethodReturnValue(BeanFactory childTrackingFactory, Object originallyCreatedBean, Method method,
			ProxyFactory pf);

}
