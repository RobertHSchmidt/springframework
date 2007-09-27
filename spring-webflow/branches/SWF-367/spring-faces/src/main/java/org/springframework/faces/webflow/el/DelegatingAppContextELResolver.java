package org.springframework.faces.webflow.el;

import javax.el.ELContext;
import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.jsf.el.DelegatingFacesELResolver;
import org.springframework.webflow.context.ExternalContextHolder;

public class DelegatingAppContextELResolver extends DelegatingFacesELResolver {

	protected WebApplicationContext getWebApplicationContext(ELContext elContext) {
		return WebApplicationContextUtils.getRequiredWebApplicationContext((ServletContext) ExternalContextHolder
				.getExternalContext().getContext());
	}

}
