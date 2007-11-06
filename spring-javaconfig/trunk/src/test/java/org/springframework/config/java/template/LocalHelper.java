package org.springframework.config.java.template;

import org.apache.commons.dbcp.BasicDataSource;

public abstract class LocalHelper {

	public static BasicDataSource basicDataSource(Class<?> driverClass, String url, String uname, String pwd) {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(driverClass.getName());
		ds.setAccessToUnderlyingConnectionAllowed(true);
		ds.setDefaultAutoCommit(false);

		// TODO pull out into wellknown properties file?
		ds.setUrl(url);
		ds.setUsername(uname);
		ds.setPassword(pwd);
		return ds;
	}

}
