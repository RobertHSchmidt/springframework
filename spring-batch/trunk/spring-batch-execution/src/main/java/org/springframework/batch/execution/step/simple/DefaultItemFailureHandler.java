/*
 * Copyright 2006-2008 the original author or authors.
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
package org.springframework.batch.execution.step.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.domain.ItemFailureHandler;

/**
 * Default implementation of the {@link ItemFailureHandler} interface that
 * writes all exceptions via commons logging. Since generics can't be used to
 * ensure the list contains exceptions, any non exceptions will be logged out by
 * calling toString on the object.
 * 
 * @author Lucas Ward
 * 
 */
public class DefaultItemFailureHandler implements ItemFailureHandler {

	protected static final Log logger = LogFactory
			.getLog(DefaultItemFailureHandler.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.domain.ItemFailureLog#log(java.util.List)
	 */
	public void handleReadFailure(Exception ex) {
		try {
			logger.error("Error encountered while reading", ex);
		} catch (Exception exception) {
			logger.error("Invalid type for logging: [" + exception.toString()
					+ "]");
		}
	}

	public void handleWriteFailure(Object item, Exception ex) {
		try {
			logger.error("Error encountered while writing item: [ " + item + "]", ex);
		} catch (Exception exception) {
			logger.error("Invalid type for logging: [" + exception.toString()
					+ "]");
		}
	}

}
