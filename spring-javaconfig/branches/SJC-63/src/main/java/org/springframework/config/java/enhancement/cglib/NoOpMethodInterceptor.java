package org.springframework.config.java.enhancement.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

/**
 * No-op adapter
 * 
 * @author Chris Beams
 */
public class NoOpMethodInterceptor implements JavaConfigMethodInterceptor {

	public boolean understands(Method candidateMethod) {
		return true;
	}

	public Object intercept(Object targetObject, Method targetMethod, Object[] args, MethodProxy methodProxy)
			throws Throwable {
		return methodProxy.invokeSuper(targetObject, args);
	}

	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	public int compareTo(JavaConfigMethodInterceptor that) {
		return this.getOrder() - that.getOrder();
	}
}
