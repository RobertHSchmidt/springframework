package org.springframework.beans.factory.java;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.jdbcDriver;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;
import org.springframework.beans.factory.java.template.AbstractHibernateConfiguration;
import org.springframework.beans.factory.java.template.HibernateOptions;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

public class HibernateTests extends TestCase {
	
	private ConfigurationListenerRegistry clr = new DefaultConfigurationListenerRegistry();
	
	public void testStoreEntity() {

		//ConfigurationProcessor.BeanNameTrackingDefaultListableBeanFactory.clear();
		
		GenericApplicationContext bf = new GenericApplicationContext();
		
		ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(
				bf, clr);
		configurationProcessor.process(HibernateTestConfig1.class);
		bf.refresh();
		
		PersonService service = (PersonService) bf.getBean("personService");
		Person adrian = new Person();
		adrian.setAge(34);
		adrian.setName("Adrian");
		service.create(adrian);
	}
	
	
	
	@Configuration(defaultAutowire = Autowire.BY_TYPE)
	@HibernateOptions(propertiesLocation="org/springframework/beans/factory/java/template/hib1.properties",
			showSql=true,
			configClasses={Person.class})
			////configLocations={"org/springframework/beans/factory/java/Person.hbm.xml"},
	public static class HibernateTestConfig1 extends AbstractHibernateConfiguration {
		
		public HibernateTestConfig1() {
			super("org/springframework/beans/factory/java/Person.hbm.xml");
		}

		@Override
		// TODO superclass injected
		public DataSource dataSource() {
			BasicDataSource bsd = new BasicDataSource();
			bsd.setDriverClassName(jdbcDriver.class.getName());
			bsd.setUsername("sa");
			bsd.setUrl("jdbc:hsqldb:mem:xdb");
			return bsd;
		}
		
		// Allow injection from XML
//		public DataSource dataSource() {
//			return dataSource;
//		}
//		
//		public void setDatasource(DataSource ds) {
//			this.dataSource = ds;
//		}
		
		@Bean//(lazy=true)
		public PersonDao personDao() {
			PersonDao dao = new PersonDao();
			return dao;
		}
		
		@Bean//(lazy=true)
		public PersonService personService() {
			return new PersonService();
		}
	}
	
	
	
	public static class PersonDao extends HibernateDaoSupport {
		public void save(Person person) {
			getHibernateTemplate().save(person);
		}
		
		public Person get(Person person, long id) {
			return (Person) getHibernateTemplate().load(Person.class, id);
		}
	}

	@Transactional
	public static class PersonService {
		private PersonDao dao;
		private SimpleJdbcTemplate simpleJdbcTemplate;
		public void setPersonDao(PersonDao dao) {
			this.dao = dao;
		}
		public void setDatasource(DataSource ds) {
			this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
		}
		
		public void create(Person p) {
			dao.getSessionFactory();
			assertEquals(0, simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM T_PERSON WHERE ID=?", p.getId()));
			dao.save(p);
			dao.getHibernateTemplate().flush();
			assertEquals(1, simpleJdbcTemplate.queryForInt("SELECT COUNT(0) FROM T_PERSON WHERE ID=?", p.getId()));
		}
	}
}
