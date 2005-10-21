/**
 * 
 */
package org.springframework.webflow.config;

import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.webflow.access.ChainedFlowLocator;
import org.springframework.webflow.access.FlowLocator;

public class BeanFactoryFlowLocatorFinder {
	private ListableBeanFactory beanFactory;

	public BeanFactoryFlowLocatorFinder(BeanFactory beanFactory) {
		this.beanFactory = (ListableBeanFactory)beanFactory;
	}

	public FlowLocator getFlowLocator() {
		Map locators = beanFactory.getBeansOfType(FlowLocator.class);
		if (locators == null || locators.isEmpty()) {
			throw new IllegalArgumentException("No flow locators are loaded in bean factory: " + beanFactory);
		}
		if (locators.size() == 1) {
			return (FlowLocator)locators.values().iterator().next();
		}
		else {
			return new ChainedFlowLocator((FlowLocator[])locators.values().toArray(new FlowLocator[0]));
		}
	}
}