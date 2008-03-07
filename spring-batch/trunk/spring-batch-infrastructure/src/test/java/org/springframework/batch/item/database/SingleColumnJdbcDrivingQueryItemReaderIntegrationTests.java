package org.springframework.batch.item.database;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.support.SingleColumnJdbcKeyGenerator;

public class SingleColumnJdbcDrivingQueryItemReaderIntegrationTests extends AbstractJdbcItemReaderIntegrationTests {

	protected ItemReader source;


	/**
	 * @return input source with all necessary dependencies set
	 */
	protected ItemReader createItemReader() throws Exception {

		SingleColumnJdbcKeyGenerator keyStrategy = new SingleColumnJdbcKeyGenerator(getJdbcTemplate(),
				"SELECT ID from T_FOOS order by ID");
		keyStrategy.setRestartSql("SELECT ID from T_FOOS where ID > ? order by ID");
		DrivingQueryItemReader inputSource = new DrivingQueryItemReader();
		inputSource.setKeyGenerator(keyStrategy);
		inputSource.setSaveState(true);
		return new FooItemReader(inputSource, getJdbcTemplate());

	}
}
