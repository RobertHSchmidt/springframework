package org.springframework.beans.factory.support;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;


/**
 * TargetSource that delegates to an arbitrary named bean at runtime,
 * performing lookup just before invocation.
 * Allows late binding of an object when building a bean factory.
 * <br>
 * Not currently used.
 * 
 * @author Rod Johnson
 *
 */
public class DelegatingTargetSource implements TargetSource {
	
	public static Object createDelegate(Class targetClass, BeanFactory beanFactory, String beanName) {
		ProxyFactory pf = new ProxyFactory(new Class[] { targetClass});
		pf.setTargetSource(new DelegatingTargetSource(targetClass, beanFactory, beanName));
		return pf.getProxy();
	}

	
	private final Class targetClass;
	private final BeanFactory beanFactory;
	private final String beanName;
	
	public DelegatingTargetSource(Class targetClass, BeanFactory beanFactory, String beanName) {
		this.targetClass = targetClass;
		this.beanFactory = beanFactory;
		this.beanName = beanName;
	}

	public Class getTargetClass() {
		return this.targetClass;
	}

	public boolean isStatic() {
		return false;
	}

	public Object getTarget() throws Exception {
		return this.beanFactory.getBean(this.beanName);
	}

	public void releaseTarget(Object target) throws Exception {
		// Nothing to do
	}
}
