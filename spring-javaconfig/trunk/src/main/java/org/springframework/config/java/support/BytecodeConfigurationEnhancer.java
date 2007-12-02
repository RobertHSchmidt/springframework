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
package org.springframework.config.java.support;

/**
 * Decorate the given configuration class for usage inside Spring application
 * context.
 * 
 * <p/> The created subclass will behave correctly with AOP weaving and
 * singleton caching. For example, the original class will return a new instance
 * on every call to a bean() method that has singleton scope. The subclass will
 * cache that and also perform AOP weaving.
 * 
 * @author Costin Leau
 * 
 */
public interface BytecodeConfigurationEnhancer {

	/**
	 * Create a new <i>enhanced</i> subclass of the given configuration
	 * instance and class.
	 * @param configurationClass configuration class - mandatory
	 * 
	 * 
	 * @return the enhanced class
	 */
	<T> Class<? extends T> enhanceConfiguration(Class<T> configurationClass);
}
