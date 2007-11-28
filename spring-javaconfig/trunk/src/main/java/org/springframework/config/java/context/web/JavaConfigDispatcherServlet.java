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
package org.springframework.config.java.context.web;

import org.springframework.web.servlet.DispatcherServlet;

/**
 * TODO: test
 * 
 * <p/>TODO: Document
 * 
 * @author Chris Beams
 */
@SuppressWarnings("serial")
public class JavaConfigDispatcherServlet extends DispatcherServlet {
	/**
	 * Returns {@link JavaConfigWebApplicationContext} unless the user has specified a
	 */
	@Override
	public Class<?> getContextClass() {
		Class<?> clazz = super.getContextClass();

		// if the user has supplied something other than the default, immediately return
		if (!clazz.equals(DEFAULT_CONTEXT_CLASS) && !clazz.equals(JavaConfigWebApplicationContext.class))
			return clazz;

		// by default, return JCWAC
		return JavaConfigWebApplicationContext.class;
	}

}
