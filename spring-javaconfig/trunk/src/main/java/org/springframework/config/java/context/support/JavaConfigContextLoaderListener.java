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
package org.springframework.config.java.context.support;

import javax.servlet.ServletContext;

import org.springframework.config.java.context.JavaConfigWebApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Convenience subclass of
 * {@link org.springframework.web.context.ContextLoaderListener} for use when
 * bootstrapping JavaConfig within web.xml. Registers
 * {@link JavaConfigWebApplicationContext} as the <code>contextClass</code> so
 * the user doesn't have to explicitly specify it within <code>web.xml</code>
 * 
 * @see JavaConfigDispatcherServlet
 * @author Chris Beams
 */
public class JavaConfigContextLoaderListener extends ContextLoaderListener {
	/**
	 * Ensures {@link JavaConfigWebApplicationContext} will be the default
	 * context class used, eliminating the need for the user to supply a
	 * 'contextClass' init-param in the web.xml
	 * 
	 * <p/>Note that if the user does explicitly set an init-param named
	 * 'contextClass' control will be immediately returned to the default
	 * implementation
	 * 
	 * @throws IllegalArgumentException if the user does not supply an
	 * init-param named 'contextConfigLocation'. It is required to supply this
	 * as there is no reasonable default to assume for the fully-qualified
	 * location of a Spring JavaConfig class.
	 * 
	 * @see JavaConfigWebApplicationContext
	 */
	@Override
	protected ContextLoader createContextLoader() {
		return new ContextLoader() {
			@Override
			protected Class<?> determineContextClass(ServletContext servletContext) throws ApplicationContextException {

				String contextClass = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
				if (contextClass != null && !contextClass.equals(JavaConfigWebApplicationContext.class.getName()))
					return super.determineContextClass(servletContext);

				if (servletContext.getInitParameter(CONFIG_LOCATION_PARAM) == null)
					throw new IllegalArgumentException(String.format(
							"A %s <init-param/> must be explicitly supplied when using %s as %s",
							CONFIG_LOCATION_PARAM, this.getClass().getSimpleName(), CONTEXT_CLASS_PARAM));

				return JavaConfigWebApplicationContext.class;
			}
		};
	}
}
