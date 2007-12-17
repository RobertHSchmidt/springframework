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
package org.springframework.config.java.context;

import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

import java.util.Map;

import org.springframework.beans.BeansException;

/**
 * Exception thrown when a BeanFactory is asked for a bean by type for which
 * there exists more than one match.
 * 
 * <p/>Note: currently used only by
 * {@link org.springframework.config.java.context.JavaConfigApplicationContext} -
 * may potentially be promoted and used more widely in the future
 * 
 * <p/>XXX: Review
 * 
 * @see org.springframework.config.java.context.JavaConfigApplicationContext#getBean(Class)
 * @author Chris Beams
 */
@SuppressWarnings("serial")
class AmbiguousBeanLookupException extends BeansException {

	public AmbiguousBeanLookupException(Class<?> type, Map<String, Object> beansOfType) {
		super(formatMessage(type, beansOfType));
	}

	public AmbiguousBeanLookupException(String message) {
		super(message);
	}

	private static String formatMessage(Class<?> type, Map<String, Object> beansOfType) {
		return String.format("%d beans match requested type [%s] - "
				+ "consider using getBean(T, String) to disambiguate. " //
				+ "Matching bean names are: [%s]", //
				beansOfType.size(), type, collectionToCommaDelimitedString(beansOfType.keySet()));
	}

}
