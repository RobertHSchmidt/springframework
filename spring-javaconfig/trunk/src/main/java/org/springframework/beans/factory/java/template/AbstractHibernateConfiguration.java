package org.springframework.beans.factory.java.template;

import java.io.IOException;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class AbstractHibernateConfiguration extends
		AbstractTransactionalConfiguration implements ResourceLoaderAware {
	
	private String[] configLocations;
	
	// TODO move to annotation or externalize into properties
	
	private ResourceLoader resourceLoader;
	
	protected AbstractHibernateConfiguration(String ... locations) {
		setConfigLocations(locations);
	}
	
	// TODO add mapped classes?
	
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public void setConfigLocations(String ... locations) {
		this.configLocations = new String[locations.length];
		int i = 0;
		for (String configLocation : locations) {
			this.configLocations[i++] = configLocation;
		}
	}

	@Bean(autowire=Autowire.NO)
	public SessionFactory sessionFactory() {
		if (this.configLocations.length == 0) {
			throw new IllegalStateException("No config locations");
		}
		
		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
		lsfb.setDataSource(dataSource());
			
		HibernateOptions ho = getClass().getAnnotation(HibernateOptions.class);
		if (ho == null) {
			throw new BeanDefinitionStoreException(getClass().getName() + " must define HibernateOptions annotation");
		}
		
		// TODO pull out into loadProperties() in superclass
		Resource resource = this.resourceLoader.getResource(ho.propertiesLocation());
		if (resource == null) {
			throw new BeanDefinitionStoreException("Cannot load properties '" + ho.propertiesLocation() + "'");
		}
		Properties props = new Properties();
		
		// Set typed properties that will be overridden by properties file
		props.setProperty("hibernate.show_sql", Boolean.toString(ho.showSql()));
		
		try {
			props.load(resource.getInputStream());
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Cannot load properties file", ex);
		}
		
		System.out.println("Using properties " + props);
		lsfb.setHibernateProperties(props);
		
		lsfb.setMappingResources(configLocations);
		
		//lsfb.setMappingResources(ho.configLocations());
		
		return (SessionFactory) getObject(lsfb);
	}

	@Override
	public PlatformTransactionManager transactionManager() {
		HibernateTransactionManager htm = new HibernateTransactionManager(sessionFactory());
		return htm;
	}
}
