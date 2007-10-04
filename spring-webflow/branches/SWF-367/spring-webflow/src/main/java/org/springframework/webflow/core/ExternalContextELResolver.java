package org.springframework.webflow.core;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.ExternalContextHolder;

public class ExternalContextELResolver extends ELResolver {

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		return Object.class;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {

		if (base == null) {
			ExternalContext externalContext = ExternalContextHolder.getExternalContext();
			// Search the scope maps for the bean
			String attributeName = property.toString();
			if (externalContext.getRequestMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getRequestMap().get(attributeName).getClass();
			} else if (externalContext.getSessionMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getSessionMap().get(attributeName).getClass();
			} else if (externalContext.getApplicationMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getApplicationMap().get(attributeName).getClass();
			}
		}
		return null;
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base == null) {
			ExternalContext externalContext = ExternalContextHolder.getExternalContext();
			// Search the scope maps for the bean
			String attributeName = property.toString();
			if (externalContext.getRequestMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getRequestMap().get(attributeName);
			} else if (externalContext.getSessionMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getSessionMap().get(attributeName);
			} else if (externalContext.getApplicationMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				return externalContext.getApplicationMap().get(attributeName);
			}
		}
		return null;
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		return false;
	}

	public void setValue(ELContext elContext, Object base, Object property, Object value) {
		if (base == null) {
			ExternalContext externalContext = ExternalContextHolder.getExternalContext();
			// Search the scope maps for the bean
			String attributeName = property.toString();
			if (externalContext.getRequestMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				externalContext.getRequestMap().put(attributeName, value);
			} else if (externalContext.getSessionMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				externalContext.getSessionMap().put(attributeName, value);
			} else if (externalContext.getApplicationMap().contains(attributeName)) {
				elContext.setPropertyResolved(true);
				externalContext.getApplicationMap().put(attributeName, value);
			}
		}
	}

}
