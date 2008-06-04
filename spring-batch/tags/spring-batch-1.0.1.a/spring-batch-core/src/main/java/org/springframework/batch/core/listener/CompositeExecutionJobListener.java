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
package org.springframework.batch.core.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * @author Dave Syer
 * 
 */
public class CompositeExecutionJobListener implements JobExecutionListener {

	private List listeners = new ArrayList();

	/**
	 * Public setter for the listeners.
	 * 
	 * @param listeners
	 */
	public void setListeners(JobExecutionListener[] listeners) {
		this.listeners = Arrays.asList(listeners);
	}

	/**
	 * Register additional listener.
	 * 
	 * @param jobExecutionListener
	 */
	public void register(JobExecutionListener jobExecutionListener) {
		if (!listeners.contains(jobExecutionListener)) {
			listeners.add(jobExecutionListener);
		}
	}

	public void afterJob(JobExecution jobExecution) {
		for (Iterator iterator = listeners.listIterator(); iterator.hasNext();) {
			JobExecutionListener listener = (JobExecutionListener) iterator.next();
			listener.afterJob(jobExecution);
		}
	}

	public void beforeJob(JobExecution jobExecution) {
		for (Iterator iterator = listeners.listIterator(); iterator.hasNext();) {
			JobExecutionListener listener = (JobExecutionListener) iterator.next();
			listener.beforeJob(jobExecution);
		}
	}

	public void onError(JobExecution jobExecution, Throwable e) {
		for (Iterator iterator = listeners.listIterator(); iterator.hasNext();) {
			JobExecutionListener listener = (JobExecutionListener) iterator.next();
			listener.onError(jobExecution, e);
		}
		
	}

	public void onInterrupt(JobExecution jobExecution) {
		for (Iterator iterator = listeners.listIterator(); iterator.hasNext();) {
			JobExecutionListener listener = (JobExecutionListener) iterator.next();
			listener.onInterrupt(jobExecution);
		}
		
	}
}
