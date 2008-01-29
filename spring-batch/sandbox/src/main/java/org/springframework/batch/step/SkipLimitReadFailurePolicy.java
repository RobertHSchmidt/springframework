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
package org.springframework.batch.step;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ben Hale
 */
public class SkipLimitReadFailurePolicy implements ReadFailurePolicy {

	private static final String SKIP_COUNT = "SKIP_COUNT";

	private final Long skipLimit;

	private final List ignoredExceptions;

	public SkipLimitReadFailurePolicy(Long skipLimit) {
		this(skipLimit, new ArrayList(0));
	}

	public SkipLimitReadFailurePolicy(Long skipLimit, List ignoredExceptions) {
		this.skipLimit = skipLimit;
		this.ignoredExceptions = ignoredExceptions;
	}

	public synchronized void registerFailure(Exception exception, ReadContext readContext) {
		if (!ignoredExceptions.contains(exception.getClass())) {
			incrementSkipCount(readContext);
		}
	}

	public synchronized boolean shouldContinue(ReadContext readContext) {
		return !(getSkipCount(readContext).compareTo(skipLimit) >= 0);
	}

	private void incrementSkipCount(ReadContext readContext) {
		Long skipCount = getSkipCount(readContext);
		putSkipCount(Long.valueOf(skipCount.longValue() + 1), readContext);
	}

	private Long getSkipCount(ReadContext readContext) {
		if (!readContext.containsKey(SKIP_COUNT)) {
			readContext.put(SKIP_COUNT, Long.valueOf(0));
		}
		return (Long) readContext.get(SKIP_COUNT);
	}

	private void putSkipCount(Long skipCount, ReadContext readContext) {
		readContext.put(SKIP_COUNT, skipCount);
	}

	public ReadFailureException getException(ReadContext readContext) {
		return new SkipLimitExceededException(skipLimit);
	}

}
