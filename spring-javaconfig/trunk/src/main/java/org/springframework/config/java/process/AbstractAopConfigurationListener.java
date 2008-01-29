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

package org.springframework.config.java.process;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.Ordered;

/**
 * ConfigurationMethodListener that saves up pointcuts, adding advices if
 * necessary to a ProxyFactory.
 * 
 * @author Rod Johnson
 */
abstract class AbstractAopConfigurationListener extends ConfigurationListenerSupport {

	/**
	 * Map from advice bean names in child factory to Pointcut definition.
	 */
	private Map<String, Pointcut> pointcuts = new HashMap<String, Pointcut>();

	/**
	 * Add an advice with the given pointcut
	 * @param adviceName bean name of the advice in the child factory
	 * @param cp ConfigurationProcessor
	 * @param childBeanFactory child factory
	 */
	protected void addAdvice(String adviceName, Pointcut pc, ConfigurationProcessor cp) {
		pointcuts.put(adviceName, pc);
	}

	protected void addAdvice(String adviceName, Pointcut pc, Advice advice, ConfigurationProcessor cp) {
		cp.registerSingleton(adviceName, advice, true);
		pointcuts.put(adviceName, pc);
	}

	@Override
	public boolean processBeanMethodReturnValue(BeanFactory childBeanFactory, Object originallyCreatedBean,
			Method method, ProxyFactory pf) {
		int added = 0;
		for (String adviceName : pointcuts.keySet()) {
			Pointcut pc = pointcuts.get(adviceName);
			if (AopUtils.canApply(pc, originallyCreatedBean.getClass())) {
				Advice advice = (Advice) childBeanFactory.getBean(adviceName);
				DefaultPointcutAdvisor a = new DefaultPointcutAdvisor(pc, advice);

				// Order advisors if necessary
				if (pc instanceof Ordered) {
					a.setOrder(((Ordered) pc).getOrder());
					int insertionPos = 0;
					for (Advisor ad : pf.getAdvisors()) {
						if (!(ad instanceof Ordered)) {
							break;
						}
						if (((Ordered) ad).getOrder() < a.getOrder()) {
							++insertionPos;
						}
						else {
							break;
						}
					}
					pf.addAdvisor(insertionPos, a);
				}
				else {
					pf.addAdvisor(a);
				}
				++added;
			}
		}
		return added > 0;
	}
}
