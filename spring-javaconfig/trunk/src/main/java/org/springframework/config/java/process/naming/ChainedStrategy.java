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
package org.springframework.config.java.process.naming;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * Chain-like implementation naming strategy.
 * 
 * Allows a stack of strategies to be specified, returning the first non-null
 * name. The default chain includes {@link BeanAnnotationNameStrategy} and
 * {@link MethodNameStrategy}.
 * 
 * @author Costin Leau
 * 
 */
public class ChainedStrategy implements BeanNamingStrategy {

	private static final BeanNamingStrategy[] DEFAULT_STRATEGIES = new BeanNamingStrategy[] {
			new BeanAnnotationNameStrategy(), new MethodNameStrategy() };

	private BeanNamingStrategy[] strategies;

	public ChainedStrategy() {
		this(DEFAULT_STRATEGIES);
	}

	public ChainedStrategy(BeanNamingStrategy[] strategies) {
		Assert.notEmpty(strategies, "at least one strategy required");
		this.strategies = strategies;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.config.java.process.naming.BeanNamingStrategy#getBeanName(java.lang.reflect.Method,
	 * org.springframework.config.java.annotation.Configuration)
	 */
	public String getBeanName(Method beanCreationMethod, Configuration configuration) {
		for (BeanNamingStrategy strategy : strategies) {
			if (strategy != null) {
				String name = strategy.getBeanName(beanCreationMethod, configuration);
				if (name != null)
					return name;
			}
		}

		throw new IllegalArgumentException("no strategy returned a name; consider using different naming strategies "
				+ Arrays.toString(strategies));
	}

}
