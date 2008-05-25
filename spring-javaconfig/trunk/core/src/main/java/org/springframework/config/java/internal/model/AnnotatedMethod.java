package org.springframework.config.java.internal.model;

import java.lang.annotation.Annotation;

interface AnnotatedMethod<A extends Annotation> {
	A getMetadata();
}
