/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.lang.annotation.Annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;

/**
 * Abstract base for {@link org.springframework.ws.server.EndpointMapping} implementations that map classes tagged with
 * an annotation. By default the annotation is {@link Endpoint}, but this can be overriden in subclasses.
 * <p/>
 * The methods of each bean carrying @Endpoint will be registered using {@link #registerMethods(Object)}.
 *
 * @author Arjen Poutsma
 * @since 1.0
 */
public abstract class AbstractAnnotationMethodEndpointMapping extends AbstractMethodEndpointMapping
        implements BeanPostProcessor {

    public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (getEndpointClass(bean).getAnnotation(getEndpointAnnotationType()) != null) {
            registerMethods(bean);
        }
        return bean;
    }

    /** Returns the 'endpoint' annotation type. Default is {@link Endpoint}. */
    protected Class<? extends Annotation> getEndpointAnnotationType() {
        return Endpoint.class;
    }

    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
