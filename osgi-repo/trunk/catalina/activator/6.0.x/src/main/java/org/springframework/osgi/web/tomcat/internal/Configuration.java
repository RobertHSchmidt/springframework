/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.web.tomcat.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * @author Costin Leau
 * 
 */
public class Configuration {

	private String home;
	private String host;
	private int port;

	private static final String HOST_KEY = "host";
	private static final String HOME_KEY = "home";
	private static final String PORT_KEY = "port";


	/**
	 * Constructs a new <code>Configuration</code> instance using the given
	 * properties.
	 * 
	 * @param userProps
	 */
	public Configuration(Properties userProps) {
		home = userProps.getProperty(HOME_KEY);
		host = userProps.getProperty(HOST_KEY);
		try {
			host = InetAddress.getByName(host).getHostAddress();
		}
		catch (UnknownHostException ex) {
			throw new IllegalArgumentException("unknown host " + host, ex);
		}
		port = Integer.valueOf(userProps.getProperty(PORT_KEY)).intValue();
	}

	/**
	 * Returns the host.
	 * 
	 * @return Returns the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port.
	 * 
	 * @return Returns the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the home.
	 * 
	 * @return Returns the home
	 */
	public String getHome() {
		return home;
	}
}
