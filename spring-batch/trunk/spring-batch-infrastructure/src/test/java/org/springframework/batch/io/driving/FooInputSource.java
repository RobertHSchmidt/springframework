package org.springframework.batch.io.driving;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.StreamContext;
import org.springframework.batch.item.reader.AbstractItemReader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

class FooItemReader extends AbstractItemReader implements ItemStream, ItemReader, DisposableBean, InitializingBean{

	DrivingQueryItemReader inputSource;
	FooDao fooDao = new SingleKeyFooDao();

	public FooItemReader(DrivingQueryItemReader inputSource, JdbcTemplate jdbcTemplate) {
		this.inputSource = inputSource;
		fooDao.setJdbcTemplate(jdbcTemplate);
	}

	public Object read() {
		Object key = inputSource.read();
		if(key != null){
			return fooDao.getFoo(key);
		}else{
			return null;
		}
	}

	public StreamContext getStreamContext() {
		return inputSource.getStreamContext();
	}

	public void restoreFrom(StreamContext data) {
		inputSource.restoreFrom(data);
	}

	public void destroy() throws Exception {
		inputSource.destroy();
	}

	public void setFooDao(FooDao fooDao) {
		this.fooDao = fooDao;
	}

	public void afterPropertiesSet() throws Exception {
	}

	public void open() {
	};

	public void close() {
	};
}
