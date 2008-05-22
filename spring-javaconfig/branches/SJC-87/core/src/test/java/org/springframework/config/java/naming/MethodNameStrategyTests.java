/*
 * Copyright 2002-2008 the original author or authors.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.config.java.naming.MethodNameStrategy.Prefix;

/**
 * @author Costin Leau
 * @author Chris Beams
 */
public class MethodNameStrategyTests extends AbstractNamingStrategyTests {

	@Override
	protected BeanNamingStrategy createNamingStrategy() {
		return new MethodNameStrategy();
	}

	@Test
	public void testDefaultConfiguration() {
		assertEquals(sampleMethod.getName(), strategy.getBeanName(sampleMethod));
	}

	@Test
	public void testClass() {
		((MethodNameStrategy) strategy).setPrefix(Prefix.CLASS);
		assertEquals(expectedClassName.concat(".").concat(expectedMethodName), strategy
				.getBeanName(sampleMethod));
	}

	@Test
	public void testFQN() {
		((MethodNameStrategy) strategy).setPrefix(Prefix.FQN);
		assertEquals(expectedFqClassName.concat(".").concat(expectedMethodName), strategy.getBeanName(sampleMethod));
	}

}
