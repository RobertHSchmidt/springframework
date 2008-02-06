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
package org.springframework.config.java.context;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

final class TypeSafeBeanFactoryUtils {

	@SuppressWarnings("unchecked")
	public static <T> T getBean(ListableBeanFactory beanFactory, Class<T> type) {
		Map<String, Object> beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type);
		int matchingBeanCount = beansOfType.size();

		// happy path -- there is exactly one matching bean: return it.
		if (matchingBeanCount == 1)
			return (T) beansOfType.values().iterator().next();

		// no matches: throw.
		if (matchingBeanCount == 0)
			throw new NoSuchBeanDefinitionException(type, "");

		// there is more than one instance: attempt to find a primary bean
		ArrayList<String> primaryCandidates = new ArrayList<String>();
		for (String beanName : beansOfType.keySet()) {
			// XXX: Review - having to cast here is odd; there's probably a
			// better way
			AbstractBeanDefinition beanDef = (AbstractBeanDefinition) ((ConfigurableListableBeanFactory) beanFactory)
					.getBeanDefinition(beanName);
			if (beanDef.isPrimary()) {
				primaryCandidates.add(beanName);
			}
		}

		int primaryCandidateCount = primaryCandidates.size();

		if (primaryCandidateCount == 0) {
			throw new AmbiguousBeanLookupException(type, beansOfType);
		}
		if (primaryCandidateCount > 1) {
			throw new MultiplePrimaryBeanDefinitionException(type, primaryCandidates);
		}
		// exactly one primary candidate found
		return (T) beanFactory.getBean(primaryCandidates.get(0));
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(ListableBeanFactory beanFactory, Class<T> type, String beanName) {
		return (T) beanFactory.getBean(beanName, type);
	}
}
