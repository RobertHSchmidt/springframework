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
package org.springframework.config.java.naming;

import org.springframework.config.java.naming.BeanNamingStrategy;
import org.springframework.config.java.naming.MethodNameStrategy;
import org.springframework.config.java.naming.MethodNameStrategy.Prefix;

/**
 * @author Costin Leau
 * 
 */
public class MethodNameStrategyTests extends AbstractNamingStrategyTests {

	@Override
	protected BeanNamingStrategy createNamingStrategy() {
		return new MethodNameStrategy();
	}

	public void testDefaultConfiguration() {
		assertEquals(sampleMethod.getName(), strategy.getBeanName(sampleMethod));
	}

	public void testClass() {
		((MethodNameStrategy) strategy).setPrefix(Prefix.CLASS);
		assertEquals(AbstractNamingStrategyTests.class.getSimpleName().concat(".setUp"),
			strategy.getBeanName(sampleMethod));
	}

	public void testFQN() {
		((MethodNameStrategy) strategy).setPrefix(Prefix.FQN);
		assertEquals(AbstractNamingStrategyTests.class.getName().concat(".setUp"), strategy.getBeanName(sampleMethod));
	}

}
