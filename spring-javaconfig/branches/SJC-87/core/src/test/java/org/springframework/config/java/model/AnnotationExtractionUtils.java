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

import java.lang.annotation.Annotation;

public class AnnotationExtractionUtils {

	public static <A extends Annotation> A extractMethodAnnotation(Class<A> targetAnno, Class<? extends MethodAnnotationPrototype> prototype) {
		try {
			return prototype.getDeclaredMethod("targetMethod").getAnnotation(targetAnno);
		} catch (Exception ex) { throw new RuntimeException(ex); }
	}

	public static <A extends Annotation> A extractClassAnnotation(Class<A> targetAnno, Class<?> prototype) {
		try {
			return prototype.getAnnotation(targetAnno);
		} catch (Exception ex) { throw new RuntimeException(ex); }
	}

}