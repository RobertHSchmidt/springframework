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

package org.springframework.config.java.complex;

import java.awt.Point;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.Lazy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Rod Johnson
 */
@Configuration
public class ComplexConfiguration {

	@Bean
	public Point topLevelBean() {
		return new Point();
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@Configuration(defaultLazy = Lazy.UNSPECIFIED, defaultAutowire = Autowire.INHERITED, useFactoryAspects = false)
	public static class InnerConfiguration {
		@Bean
		public Object myBean() {
			return new Object();
		}
	}

	@Configuration
	public static class DeepConfiguration {

		// @Configuration(names = { "test", "database" })
		// public class VeryDeepConfiguration {
		// public void anonymousClass() {
		// new InnerConfiguration() {
		// };
		// }
		// }

		@Bean
		public ITestBean deepConfigurationBean() {
			return new TestBean();
		}
	}
}
