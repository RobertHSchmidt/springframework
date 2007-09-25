package org.springframework.webflow.config;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.ConditionalFlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.FlowExecutionListenerCriteriaFactory;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;

class FlowExecutionListenerLoaderFactoryBean implements FactoryBean, InitializingBean {

	private Map listenersWithCriteria;

	private ConditionalFlowExecutionListenerLoader listenerLoader;

	private FlowExecutionListenerCriteriaFactory listenerCriteriaFactory = new FlowExecutionListenerCriteriaFactory();

	public void setListeners(Map listenersWithCriteria) {
		this.listenersWithCriteria = listenersWithCriteria;
	}

	public void afterPropertiesSet() {
		listenerLoader = new ConditionalFlowExecutionListenerLoader();
		Iterator it = listenersWithCriteria.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			FlowExecutionListener listener = (FlowExecutionListener) entry.getKey();
			String criteria = (String) entry.getValue();
			listenerLoader.addListener(listener, listenerCriteriaFactory.getListenerCriteria(criteria));
		}
	}

	public Object getObject() throws Exception {
		return listenerLoader;
	}

	public Class getObjectType() {
		return FlowExecutionListenerLoader.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
