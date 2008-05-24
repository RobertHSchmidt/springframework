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
package org.springframework.config.java.model;


import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.config.java.context.DefaultJavaConfigBeanFactory;
import org.springframework.core.io.Resource;

/**
 * <p/>
 * Fashioned after the {@link BeanDefinitionReader} hierarchy, but does not extend any of its artifacts
 * because differences were significant enough to merit the departure.  The primary example is that
 * this class needs to interact with a {@link DefaultJavaConfigBeanFactory} rather than a {@link BeanDefinitionRegistry}.
 * the latter was mandated by the existing {@link BeanDefinitionReader} hierarchy.
 */
public interface JavaConfigBeanDefinitionReader {
	int loadBeanDefinitions(Resource configClassResource) throws BeanDefinitionStoreException;
	int loadBeanDefinitions(Resource[] configClassResources) throws BeanDefinitionStoreException;
}