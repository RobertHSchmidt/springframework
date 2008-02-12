package org.springframework.config.java.process;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;

import org.springframework.core.Ordered;

interface JavaConfigMethodInterceptor extends MethodInterceptor, Ordered, Comparable<JavaConfigMethodInterceptor> {
	boolean understands(Method candidateMethod);
}
