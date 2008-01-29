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
package org.springframework.batch.reader;

import org.springframework.batch.io.exception.BatchCriticalException;

/**
 * Exception indicating that the skip limit for a particular step
 * has been exceeded.  This exception should always cause a job to
 * terminate abnormally.
 * 
 * @author Lucas Ward
 *
 */
public class SkipLimitExceededException extends BatchCriticalException {

	private static final long serialVersionUID = -7219854737641474304L;

	public SkipLimitExceededException(String msg) {
		super(msg);
	}
	
	public SkipLimitExceededException(String msg, Throwable t) {
		super(msg, t);
	}
}
