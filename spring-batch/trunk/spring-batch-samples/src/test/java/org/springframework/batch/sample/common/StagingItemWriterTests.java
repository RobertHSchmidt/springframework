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
package org.springframework.batch.sample.common;

import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.sample.common.StagingItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class StagingItemWriterTests {

	private JdbcOperations jdbcTemplate;

	@Autowired
	private StagingItemWriter<String> writer;

	public JdbcOperations getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Before
	public void onSetUpBeforeTransaction() throws Exception {
		StepExecution stepExecution = new StepExecution("stepName", new JobExecution(new JobInstance(new Long(12L),
				new JobParameters(), "testJob")));
		writer.beforeStep(stepExecution);
	}

	@Transactional
	@Test
	public void testProcessInsertsNewItem() throws Exception {
		int before = getJdbcTemplate().queryForInt("SELECT COUNT(*) from BATCH_STAGING");
		writer.write("FOO");
		int after = getJdbcTemplate().queryForInt("SELECT COUNT(*) from BATCH_STAGING");
		assertEquals(before + 1, after);
	}

}
