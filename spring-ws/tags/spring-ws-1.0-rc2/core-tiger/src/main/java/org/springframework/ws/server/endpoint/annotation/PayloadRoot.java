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

package org.springframework.ws.server.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an endpoint method as the handler for an incoming request. The annotation values signify the the request
 * payload root element that is handled by the method.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.server.endpoint.mapping.PayloadRootAnnotationMethodEndpointMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PayloadRoot {

    /**
     * Signifies the local part of the payload root element handled by the annotated method.
     *
     * @see #namespace()
     */
    String localPart();

    /**
     * Signifies the namespace of the payload root element handled by the annotated method.
     *
     * @see #localPart()
     */
    String namespace() default "";

}
