/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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