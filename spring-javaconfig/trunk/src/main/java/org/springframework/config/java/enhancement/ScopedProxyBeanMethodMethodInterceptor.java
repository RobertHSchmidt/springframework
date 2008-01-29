/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.config.java.enhancement;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.config.java.annotation.aop.ScopedProxy;
import org.springframework.config.java.util.ScopeUtils;
import org.springframework.util.Assert;

/**
 * CGLIB callback suitable for scoped proxies. It's aware of the name switch
 * involved with &#64;ScopedProxy.
 * 
 * @see ScopedProxy
 * @see BeanMethodMethodInterceptor
 * @see MethodInterceptor
 * @author Costin Leau
 */
class ScopedProxyBeanMethodMethodInterceptor implements MethodInterceptor {

	private final BeanMethodMethodInterceptor delegate;

	public ScopedProxyBeanMethodMethodInterceptor(BeanMethodMethodInterceptor delegate) {
		Assert.notNull(delegate, "the MethodInterceptor delegate is required");
		this.delegate = delegate;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[],
	 * net.sf.cglib.proxy.MethodProxy)
	 */
	public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
		String beanToReturn = delegate.getBeanName(m);
		String scopedBean = ScopeUtils.getScopedHiddenName(beanToReturn);

		if (delegate.isCurrentlyInCreation(scopedBean))
			beanToReturn = scopedBean;
		return delegate.returnWrappedResultMayBeCached(beanToReturn, o, m, args, mp);
	}
}
