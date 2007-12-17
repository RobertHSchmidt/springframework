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

import org.springframework.config.java.context.JavaConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Convenience subclass of
 * {@link org.springframework.web.servlet.DispatcherServlet} that registers
 * {@link JavaConfigWebApplicationContext} as the <code>contextClass</code> to
 * be used so the user doesn't need to explicitly set it in <code>web.xml</code>.
 * Otherwise behaves exactly as the superclass DispacherServlet.
 * 
 * <p/>NOTE: overriding the <code>contextClass</code> by explicitly specifying
 * an <code>init-param</code> in <code>web.xml</code> is not supported. If
 * you need to do this, simply revert to using the default
 * {@link DispatcherServlet}
 * 
 * @see JavaConfigContextLoaderListener
 * @author Chris Beams
 */
@SuppressWarnings("serial")
public class JavaConfigDispatcherServlet extends DispatcherServlet {

	{
		setContextClass(JavaConfigWebApplicationContext.class);
	}

}
