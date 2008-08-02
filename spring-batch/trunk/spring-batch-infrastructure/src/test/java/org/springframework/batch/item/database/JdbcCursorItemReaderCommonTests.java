package org.springframework.batch.item.database;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.sample.Foo;
import org.junit.runner.RunWith;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.Test;

@RunWith(JUnit4ClassRunner.class)
public class JdbcCursorItemReaderCommonTests extends CommonDatabaseItemStreamItemReaderTests {

	protected ItemReader<Foo> getItemReader() throws Exception {

		JdbcCursorItemReader<Foo> result = new JdbcCursorItemReader<Foo>();
		result.setDataSource(getDataSource());
		result.setSql("select ID, NAME, VALUE from T_FOOS");
		result.setIgnoreWarnings(true);
		result.setVerifyCursorPosition(true);

		result.setMapper(new FooRowMapper());
		result.setFetchSize(10);
		result.setMaxRows(100);
		result.setQueryTimeout(1000);
		result.setSaveState(true);
		result.setDriverSupportsAbsolute(false);

		return result;
	}

	@Test
	public void testRestartWithDriverSupportsAbsolute() throws Exception {
		tested = getItemReader();
		((JdbcCursorItemReader<Foo>) tested).setDriverSupportsAbsolute(true);
		testedAsStream().open(executionContext);

		testRestart();
	}

	protected void pointToEmptyInput(ItemReader<Foo> tested) throws Exception {
		JdbcCursorItemReader<Foo> reader = (JdbcCursorItemReader<Foo>) tested;
		reader.close(new ExecutionContext());
		reader.setSql("select ID from T_FOOS where ID < 0");
		reader.afterPropertiesSet();
		reader.open(new ExecutionContext());		
	}

}
