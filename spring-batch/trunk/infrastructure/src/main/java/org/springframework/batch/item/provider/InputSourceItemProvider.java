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

package org.springframework.batch.item.provider;

import java.util.Properties;

import org.springframework.batch.io.InputSource;
import org.springframework.batch.io.Skippable;
import org.springframework.batch.restart.RestartData;
import org.springframework.batch.restart.Restartable;
import org.springframework.batch.statistics.StatisticsProvider;

/**
 * Simple wrapper around {@link InputSource}. The input source is expected to
 * take care of open and close operations. If necessary it should be registered
 * as a step scoped bean to ensure that the lifecycle methods are called.
 * 
 * @author Dave Syer
 */
public class InputSourceItemProvider extends AbstractItemProvider implements Restartable, StatisticsProvider, Skippable {

	private InputSource source;

	/**
	 * Get the next object from the input source.
	 * @see org.springframework.batch.item.ItemProvider#next()
	 */
	public Object next() {
		Object value = source.read();
		return value;
	}

	/**
	 * @see Restartable#getRestartData()
	 * @throws IllegalStateException if the parent template is not itself
	 * {@link Restartable}.
	 */
	public RestartData getRestartData() {
		if (!(source instanceof Restartable)) {
			throw new IllegalStateException("Input Template is not Restartable");
		}
		return ((Restartable) source).getRestartData();
	}

	/**
	 * @see Restartable#restoreFrom(RestartData)
	 * @throws IllegalStateException if the parent template is not itself
	 * {@link Restartable}.
	 */
	public void restoreFrom(RestartData data) {
		if (!(source instanceof Restartable)) {
			throw new IllegalStateException("Input Template is not Restartable");
		}
		((Restartable) source).restoreFrom(data);
	}

	/**
	 * @return delegates to the parent template of it is a
	 * {@link StatisticsProvider}, otherwise returns an empty
	 * {@link Properties} instance.
	 * @see StatisticsProvider#getStatistics()
	 */
	public Properties getStatistics() {
		if (!(source instanceof StatisticsProvider)) {
			return new Properties();
		}
		return ((StatisticsProvider) source).getStatistics();
	}

	/**
	 * Setter for input source.
	 * @param source
	 */
	public void setInputSource(InputSource source) {
		this.source = source;
	}

	public void skip() {
		if (source instanceof Skippable) {
			((Skippable)source).skip();
		}
	}
}
