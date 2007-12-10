package org.springframework.beans.factory;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

public final class TypeSafeBeanFactoryUtils {

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
