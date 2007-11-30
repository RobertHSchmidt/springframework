package org.springframework.config.java;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * Simple scope implementation which creates object based on a flag.
 * 
 * @author Costin Leau
 * @author Chris Beams
 */
public class CustomScope implements Scope {

	public boolean createNewScope = true;

	private Map<String, Object> beans = new HashMap<String, Object>();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.Scope#get(java.lang.String,
	 * org.springframework.beans.factory.ObjectFactory)
	 */
	public Object get(String name, ObjectFactory objectFactory) {

		if (createNewScope) {
			beans.clear();
			// reset the flag back
			createNewScope = false;
		}

		Object bean = beans.get(name);
		// if a new object is requested or none exists under the current
		// name, create one
		if (bean == null) {
			beans.put(name, objectFactory.getObject());
		}
		return beans.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.Scope#getConversationId()
	 */
	public String getConversationId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String,
	 * java.lang.Runnable)
	 */
	public void registerDestructionCallback(String name, Runnable callback) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
	 */
	public Object remove(String name) {
		return beans.remove(name);
	}

}