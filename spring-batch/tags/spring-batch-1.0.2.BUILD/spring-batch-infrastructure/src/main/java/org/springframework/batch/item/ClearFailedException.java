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
package org.springframework.batch.item;

/**
 * Unchecked exception indicating that an error has occurred while trying to call {@link ItemWriter#clear()}
 * 
 * @author Lucas Ward
 * @author Ben Hale
 */
public class ClearFailedException extends ItemWriterException {

	/**
	 * Create a new {@link ClearFailedException} based on a message and another exception.
	 * 
	 * @param message the message for this exception
	 * @param cause the other exception
	 */
	public ClearFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new {@link ClearFailedException} based on a message.
	 * 
	 * @param message the message for this exception
	 */
	public ClearFailedException(String message) {
		super(message);
	}

}
