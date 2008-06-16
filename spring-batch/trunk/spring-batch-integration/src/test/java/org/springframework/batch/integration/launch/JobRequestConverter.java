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
package org.springframework.batch.integration.launch;

import java.util.Properties;

import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.integration.JobSupport;
import org.springframework.integration.annotation.Handler;

/**
 * @author Dave Syer
 *
 */
public class JobRequestConverter {

	@Handler
	public JobExecutionRequest convert(String jobName) {
		// TODO: get these from message header
		Properties properties = new Properties();
		return new JobExecutionRequest(new JobSupport(jobName), new DefaultJobParametersConverter().getJobParameters(properties));
	}

}
