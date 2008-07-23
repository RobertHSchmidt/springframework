package org.springframework.batch.item.database;

import org.hibernate.SessionFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.sample.Foo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class HibernateCursorItemReaderCommonTests extends CommonDatabaseItemStreamItemReaderTests {

	protected ItemReader<Foo> getItemReader() throws Exception {
		
		SessionFactory sessionFactory = createSessionFactory();

		String hsqlQuery = "from Foo";

		HibernateCursorItemReader<Foo> reader = new HibernateCursorItemReader<Foo>();
		reader.setQueryString(hsqlQuery);
		reader.setSessionFactory(sessionFactory);
		reader.setUseStatelessSession(true);
		reader.afterPropertiesSet();
		reader.setSaveState(true);

		return reader;
	}
	
	private SessionFactory createSessionFactory() throws Exception {
		LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
		factoryBean.setDataSource(getDataSource());
		factoryBean.setMappingLocations(new Resource[] { new ClassPathResource("Foo.hbm.xml", getClass()) });
		factoryBean.afterPropertiesSet();
		
		return (SessionFactory) factoryBean.getObject();

	}

	protected void pointToEmptyInput(ItemReader<Foo> tested) throws Exception {
		HibernateCursorItemReader<Foo> reader = (HibernateCursorItemReader<Foo>) tested;
		reader.close(new ExecutionContext());
		reader.setQueryString("from Foo foo where foo.id = -1");
		reader.afterPropertiesSet();
		reader.open(new ExecutionContext());
	}

}
