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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.execution.bootstrap.support.CommandLineJobRunner;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link ItemReaderExceptionHandler} interface that
 * will, by default, log all exceptions to a standard logger, and rethrow only
 * exceptions explicitly asked for.  A list of 'bad' exceptions are provided, and
 * if the provided exception is in the list, it will be rethrown.  The fully
 * qualified classname must be given, such as: org.springframework.batch.exception.
 * 
 * @author Lucas Ward
 *
 */
public class DefaultItemReaderExceptionHandler implements ItemReaderExceptionHandler{

	protected static final Log logger = LogFactory
	.getLog(DefaultItemReaderExceptionHandler.class);
	
	private final List badExceptions;
	
	public DefaultItemReaderExceptionHandler(List badExceptions) {
		
		Assert.notNull(badExceptions, "Exception list must be null.");
		this.badExceptions = badExceptions;
	}
	
	public DefaultItemReaderExceptionHandler() {
		this.badExceptions = new ArrayList();
	}
	
	public void onException(Throwable t) {
		logger.error("Exception Handled.", t);
		String exceptionName = t.getClass().getCanonicalName();
		if(badExceptions.contains(exceptionName)){
			if(t instanceof RuntimeException){
				throw (RuntimeException)t;
			}
			else{
				throw new RuntimeException(t);
			}
		}
	}

}
