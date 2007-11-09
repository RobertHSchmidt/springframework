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

package org.springframework.batch.execution.runtime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.batch.core.domain.JobIdentifier;

public class ScheduledJobIdentifier extends DefaultJobIdentifier implements JobIdentifier {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	private Date scheduleDate;
	
	ScheduledJobIdentifier() {
		this(null);
	}
	
	public ScheduledJobIdentifier(String name) {
		super(name);
		try {
			scheduleDate = dateFormat.parse("19700101");
		} catch (ParseException e) {
			throw new IllegalStateException("Could not parse trivial date 19700101");
		}
	}

	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}
	
	public String toString() {
		return super.toString() + ",scheduleDate="
				+ scheduleDate;
	}

	/**
	 * Returns true if the provided JobIdentifier equals this JobIdentifier. Two
	 * Identifiers are considered to be equal if they have the same name,
	 * stream, run, and schedule date.
	 */
	public boolean equals(Object other) {
		return EqualsBuilder.reflectionEquals(this, other) || EqualsBuilder.reflectionEquals(other, this);
	}
	
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
