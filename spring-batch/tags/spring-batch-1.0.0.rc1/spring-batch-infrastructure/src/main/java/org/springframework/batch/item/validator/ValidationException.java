/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.item.validator;

import org.springframework.batch.item.ItemReaderException;

/**
 * This exception should be thrown when there are validation errors.
 * 
 * @author Ben Hale
 */
public class ValidationException extends ItemReaderException {

	/**
	 * Create a new {@link ValidationException} based on a message and another exception.
	 * 
	 * @param message the message for this exception
	 * @param cause the other exception
	 */
	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new {@link ValidationException} based on a message.
	 * 
	 * @param message the message for this exception
	 */
	public ValidationException(String message) {
		super(message);
	}

}
