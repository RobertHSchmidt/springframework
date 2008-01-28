/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.Stack;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * BeanFactory which keeps track of getBean() calls.
 * 
 * <p/> Subclass of DefaultListableBeanFactory that keeps track of calls to
 * getBean() to allow for context-sensitive behaviour.
 * 
 * @author Rod Johnson
 * 
 */
public class BeanNameTrackingDefaultListableBeanFactory extends DefaultListableBeanFactory {

	private static ThreadLocal<Stack<String>> namesHolder = new ThreadLocal<Stack<String>>() {
		@Override
		protected Stack<String> initialValue() {
			return new Stack<String>();
		}
	};

	public static Stack<String> names() {
		return namesHolder.get();
	}

	public BeanNameTrackingDefaultListableBeanFactory(ConfigurableListableBeanFactory parent) {
		super(parent);
		copyConfigurationFrom(parent);

	}

	@Override
	public Object getBean(String name) throws BeansException {
		recordRequestForBeanName(name);
		try {
			Object result = super.getBean(name);
			return result;
		}
		finally {
			pop();
		}
	}

	public void recordRequestForBeanName(String name) {
		names().push(name);
	}

	public String pop() {
		return names().pop();
	}

	public String lastRequestedBeanName() {
		return names().empty() ? null : names().peek();
	}
}
