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
package org.springframework.config.java.internal.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.springframework.core.annotation.AnnotationUtils;

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

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> A findAnnotation(Class<A> targetType, Annotation[] annotations) {
		for(Annotation a : annotations)
			if(a.annotationType().equals(targetType))
				return (A) a; // target found -> return it

		// couldn't find the target annotation
		return null;
	}

	/**
	 * Modeled after {@link AnnotationUtils#findAnnotation(Method, Class)}, finds all
	 * annotations on a given <var>method</var> by traversing up the class hierarchy.
	 * This is necessary, because {@link Inherited @Inherited} does not apply to methods.
	 *
	 * @param method target method
	 * @return all annotations, locally defined or defined in a superimplementation of <var>method</var>
	 */
	public static Annotation[] findAnnotations(Method method) {
		return findAnnotations(method, new AnnotationFilter() {
			public boolean accept(Annotation candidate) {
				// include all annotations
				return true;
			}
		});
	}

	/**
	 * Finds all annotations on a given <var>method</var> that match <var>filter</var>.
	 * Semantics are otherwise identical to {@link #findAnnotations(Method)}
	 *
	 * @see #findAnnotations(Method)
	 *
	 * @param method target method
	 * @return all annotations on <var>method</var> that match <var>filter</var>
	 */
	public static Annotation[] findAnnotations(Method method, AnnotationFilter filter) {
		ArrayList<Annotation> annotations = new ArrayList<Annotation>();
		Class<?> cl = method.getDeclaringClass();
		while(true) {
			for(Annotation candidate : method.getDeclaredAnnotations())
				if(filter.accept(candidate))
					annotations.add(candidate);

			cl = cl.getSuperclass();
			if(cl == null)
				break;

			try {
				method = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException e) {
				break;
			}
		}

		return annotations.toArray(new Annotation[annotations.size()]);
	}

	public static interface AnnotationFilter {
		boolean accept(Annotation candidate);
	}

}