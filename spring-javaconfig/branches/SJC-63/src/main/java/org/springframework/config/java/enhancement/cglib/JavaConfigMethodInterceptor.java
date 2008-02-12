package org.springframework.config.java.enhancement.cglib;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;

import org.springframework.core.Ordered;

public interface JavaConfigMethodInterceptor extends MethodInterceptor, Ordered,
		Comparable<JavaConfigMethodInterceptor> {
	boolean understands(Method candidateMethod);
}
