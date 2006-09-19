/**
 * 
 */
package org.springframework.webflow.context.servlet;

import java.util.Map;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.springframework.webflow.core.collection.AttributeMapBindingEvent;
import org.springframework.webflow.core.collection.AttributeMapBindingListener;
import org.springframework.webflow.core.collection.LocalAttributeMap;

public class HttpSessionMapBindingListener implements HttpSessionBindingListener {
	private AttributeMapBindingListener listener;

	private Map sessionMap;
	
	public HttpSessionMapBindingListener(AttributeMapBindingListener listner, Map sessionMap) {
		this.listener = listner;
		this.sessionMap = sessionMap;
	}

	public AttributeMapBindingListener getListener() {
		return listener;
	}
	
	public void valueBound(HttpSessionBindingEvent event) {
		listener.valueBound(getContextBindingEvent(event));
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		listener.valueUnbound(getContextBindingEvent(event));
	}

	private AttributeMapBindingEvent getContextBindingEvent(HttpSessionBindingEvent event) {
		return new AttributeMapBindingEvent(new LocalAttributeMap(sessionMap), event.getName(), listener);
	}
}