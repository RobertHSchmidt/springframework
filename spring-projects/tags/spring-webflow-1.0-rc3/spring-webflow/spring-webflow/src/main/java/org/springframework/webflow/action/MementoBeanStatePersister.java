/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.action;

import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Manages bean state in a {@link Memento} stored in a {@link ScopeType}.
 * The beans persisted by this state persister are expected to be
 * {@link org.springframework.webflow.action.MementoOriginator} implementations.
 * 
 * @see org.springframework.webflow.action.MementoOriginator
 * 
 * @author Keith Donald
 */
public class MementoBeanStatePersister implements BeanStatePersister {

	private static final String BEAN_CONTEXT_ATTRIBUTE = "bean";
	
	/**
	 * The scope to expose the bean memento in. Default is
	 * {@link ScopeType#FLOW}.
	 */
	private ScopeType scope = ScopeType.FLOW;

	/**
	 * Returns the configured bean memento scope type.
	 */
	public ScopeType getScope() {
		return scope;
	}

	/**
	 * Set the configured bean memento scope type.
	 */
	public void setScope(ScopeType scope) {
		this.scope = scope;
	}

	public void saveState(Object bean, RequestContext context) {
		assertIsMementoOriginator(bean);
		getScope().getScope(context).put(
				getAttributeName(context),
				((MementoOriginator)bean).createMemento());
	}

	public Object restoreState(Object bean, RequestContext context) {
		assertIsMementoOriginator(bean);
		Memento memento = (Memento)getScope().getScope(context).getRequired(getAttributeName(context));
		((MementoOriginator)bean).setMemento(memento);
		return bean;
	}
	
	/**
	 * Make sure given bean is a {@link MementoOriginator}.
	 */
	private void assertIsMementoOriginator(Object bean) {
		Assert.isInstanceOf(MementoOriginator.class, bean,
				"MementoBeanStatePersister requires the persisted bean to be a MementoOriginator");
	}

	/**
	 * Return the attribute name of the memento in the context.
	 */
	protected String getAttributeName(RequestContext context) {
		return "memento." + context.getAttributes().getRequiredString(BEAN_CONTEXT_ATTRIBUTE);
	}
}