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
package org.springframework.config.java.support.cglib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.CallbackFilter;

import org.springframework.config.java.annotation.AutoBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.util.ClassUtils;

/**
 * 
 * Intercept only bean creation methods.
 * 
 * @author Rod Johnson
 * 
 */
public class BeanCreationCallbackFilter implements CallbackFilter {

	public int accept(Method m) {
		// We don't intercept non-public methods like finalize
		if (!Modifier.isPublic(m.getModifiers())) {
			return 0;
		}
		if (ClassUtils.hasAnnotation(m, Bean.class)) {
			return 1;
		}
		if (ClassUtils.hasAnnotation(m, ExternalBean.class) || ClassUtils.hasAnnotation(m, AutoBean.class)) {
			return 2;
		}
		return 0;
	}
}
