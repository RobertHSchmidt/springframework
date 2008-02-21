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

import org.springframework.batch.core.domain.ItemSkipPolicy;
import org.springframework.batch.core.domain.StepContribution;

/**
 * {@link ItemSkipPolicy} implementation that always returns false,
 * indicating that an item should not be skipped.
 * 
 * @author Lucas Ward
 */
public class NeverSkipItemSkipPolicy implements ItemSkipPolicy{

	public boolean shouldSkip(Exception ex, StepContribution stepContribution) {
		return false;
	}

	
}
