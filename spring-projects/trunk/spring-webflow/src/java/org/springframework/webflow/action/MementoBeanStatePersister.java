package org.springframework.webflow.action;

import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Manages bean state in a {@link Memento} stored in a {@link ScopeType}.
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

	public Object restoreState(Object bean, RequestContext context) throws Exception {
		String attributeName = context.getAttributes().getString(BEAN_CONTEXT_ATTRIBUTE);
		Memento memento = (Memento)getScope().getScope(context).getRequired(attributeName);
		((MementoOriginator)bean).setMemento(memento);
		return bean;
	}

	public void saveState(Object bean, RequestContext context) throws Exception {
		String attributeName = context.getAttributes().getString(BEAN_CONTEXT_ATTRIBUTE);
		Memento memento = ((MementoOriginator)bean).createMemento();
		getScope().getScope(context).put(attributeName, memento);
	}
}