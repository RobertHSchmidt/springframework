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
package org.springframework.batch.io.exception;

/**
 * Exception thrown when errors are encountered
 * parsing flat files.  The original input, typically
 * a line, can be passed in, so that latter catches
 * can write out the original input to a log, or
 * an error table.
 *
 * @author Lucas Ward
 *
 */
public class FlatFileParsingException extends ParsingException {

	private static final long serialVersionUID = 2529197834044942724L;

	private String input;
	private int lineNumber;

	public FlatFileParsingException(String message, String input) {
		super(message);
		this.input = input;
	}

	public FlatFileParsingException(String message, String input, int lineNumber) {
		super(message);
		this.input = input;
		this.lineNumber = lineNumber;
	}

	public FlatFileParsingException(String message, Throwable cause, String input, int lineNumber) {
		super(message, cause);
		this.input = input;
		this.lineNumber = lineNumber;
	}

	public FlatFileParsingException(Throwable cause, String input) {
		super(cause);
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public int getLineNumber() {
		return lineNumber;
	}	
}
