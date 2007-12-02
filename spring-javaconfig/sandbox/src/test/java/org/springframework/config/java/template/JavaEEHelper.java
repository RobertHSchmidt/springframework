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
package org.springframework.config.java.template;

import javax.naming.NamingException;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean;
import org.springframework.jndi.JndiObjectFactoryBean;

public abstract class JavaEEHelper {

	public static Object jndiObject(String jndiName) {
		return jndiObject(jndiName, null);
	}

	public static Object jndiObject(String jndiName, Class<?> proxyInterface) {
		JndiObjectFactoryBean jofb = new JndiObjectFactoryBean();
		jofb.setJndiName(jndiName);
		if (proxyInterface != null) {
			jofb.setProxyInterface(proxyInterface);
		}
		try {
			jofb.afterPropertiesSet();
		}
		catch (NamingException ex) {
			throw new BeanDefinitionStoreException("Cannot look up JNDI object with name '" + jndiName + "'", ex);
		}
		return jofb.getObject();
	}

	public static LocalStatelessSessionProxyFactoryBean localEjbClient(String jndiName, Class<?> businessInterface) {
		// TODO convert to return actual object
		LocalStatelessSessionProxyFactoryBean lsspfb = new LocalStatelessSessionProxyFactoryBean();
		lsspfb.setJndiName(jndiName);
		lsspfb.setBusinessInterface(businessInterface);
		return lsspfb;
	}

}
