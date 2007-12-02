/*
 * Copyright 2002-2007 the original author or authors.
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
