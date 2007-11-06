package org.springframework.config.java.template;

import javax.naming.NamingException;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean;
import org.springframework.jndi.JndiObjectFactoryBean;

public abstract class J2eeHelper {

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
