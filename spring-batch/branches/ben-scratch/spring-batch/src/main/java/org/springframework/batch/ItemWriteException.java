/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.batch;

/**
 * The exception thrown any time an error happens while writing items. This exception is generally
 * fatal.
 * 
 * @author Ben Hale
 */
public class ItemWriteException extends Exception {

	/**
	 * Constructs a new {@link ItemWriteException} with the specified detail message. The cause is
	 * not initialized, and may subsequently be initialized by a call to {@link #initCause}.
	 * 
	 * @param message the detail message. The detail message is saved for later retrieval by the
	 *            {@link #getMessage()} method.
	 */
	public ItemWriteException(String message) {
		super(message);
	}

	/**
	 * Constructs a new {@link ItemWriteException} with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is <i>not</i>
	 * automatically incorporated in this runtime exception's detail message.
	 * 
	 * @param message the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause the cause (which is saved for later retrieval by the {@link #getCause()}
	 *            method). (A <code>null</code> value is permitted, and indicates that the cause
	 *            is nonexistent or unknown.)
	 */
	public ItemWriteException(String message, Throwable cause) {
		super(message, cause);
	}

}
