package org.springframework.batch.core.resource;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.AbstractJobDaoTests;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.util.ClassUtils;

public class JdbcCursorItemReaderPreparedStatementIntegrationTests extends
	AbstractTransactionalDataSourceSpringContextTests {

	JdbcCursorItemReader itemReader;
	
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();
		
		itemReader = new JdbcCursorItemReader();
		itemReader.setDataSource(super.getJdbcTemplate().getDataSource());
		itemReader.setSql("select ID, NAME, VALUE from T_FOOS where ID > ? and ID < ?");
		itemReader.setIgnoreWarnings(true);
		itemReader.setVerifyCursorPosition(true);
		
		itemReader.setMapper(new FooRowMapper());
		itemReader.setFetchSize(10);
		itemReader.setMaxRows(100);
		itemReader.setQueryTimeout(1000);
		itemReader.setSaveState(true);
		StepExecutionPreparedStatementSetter pss = new StepExecutionPreparedStatementSetter();
		JobParameters jobParameters = new JobParametersBuilder().addLong("begin.id", new Long(1)).addLong("end.id", new Long(4)).toJobParameters();
		JobInstance jobInstance = new JobInstance(new Long(1), jobParameters, "simpleJob");
		JobExecution jobExecution = new JobExecution(jobInstance, new Long(2));
		StepExecution stepExecution = new StepExecution("taskletStep", jobExecution, new Long(3) );
		pss.beforeStep(stepExecution);
		
		List parameterNames = new ArrayList();
		parameterNames.add("begin.id");
		parameterNames.add("end.id");
		pss.setParameterKeys(parameterNames);
		
		itemReader.setPreparedStatementSetter(pss);
	}
	
	public void testRead() throws Exception{
		itemReader.open(new ExecutionContext());
		Foo foo = (Foo)itemReader.read();
		assertEquals(2, foo.getId());
		foo = (Foo)itemReader.read();
		assertEquals(3, foo.getId());
		assertNull(itemReader.read());
	}
	
	protected String[] getConfigLocations() {
		return new String[] { ClassUtils.addResourcePathToPackagePath(AbstractJobDaoTests.class, "data-source-context.xml") };
	}
}