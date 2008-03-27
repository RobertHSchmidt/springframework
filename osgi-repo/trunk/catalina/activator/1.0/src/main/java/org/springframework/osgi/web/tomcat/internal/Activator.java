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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.management.MBeanRegistration;

import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.startup.Embedded;
import org.apache.catalina.util.ServerInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Simple activator for starting Apache Tomcat Catalina container inside OSGi.
 * To avoid the need of commons digester or an XML API, this activator uses a
 * simple property file to externalize the server settings (such as hostname,
 * port and the like).
 * 
 * <p/> This activator looks initially for a
 * <code>conf/embedded-server.properties</code> file falling back to
 * <code>conf/embedded-server-defaults.properties</code>. This allows the
 * default configuration to be tweaked through fragments for example.
 * 
 * <p/> The properties file must contain the following properties:
 * 
 * <pre class="code">
 * home = [catalina home]
 * host = [server host name]
 * port = [server port number]
 * </pre>
 * 
 * @author Costin Leau
 */
public class Activator implements BundleActivator {

	/** logger */
	private static final Log log = LogFactory.getLog(Activator.class);

	/** user-configurable config location */
	private static final String CONF_LOCATION = "conf/embedded-server.properties";

	/** default config location */
	private static final String DEFAULT_CONF_LOCATION = "conf/embedded-server-defaults.properties";

	private BundleContext bundleContext;

	private Embedded server;

	private ServiceRegistration registration;

	private Thread startupThread;


	public void start(BundleContext context) throws Exception {
		this.bundleContext = context;
		// do the initialization on a different thread
		// so the activator finishes fast
		startupThread = new Thread(new Runnable() {

			public void run() {
				log.info("Starting " + ServerInfo.getServerInfo() + " ...");

				// default startup procedure
				ClassLoader cl = Activator.class.getClassLoader();
				Thread current = Thread.currentThread();
				ClassLoader old = current.getContextClassLoader();

				try {
					current.setContextClassLoader(cl);

					Configuration config = readConfiguration(bundleContext.getBundle());
					server = createCatalinaServer(config);

					server.start();
					log.info("Succesfully started " + ServerInfo.getServerInfo());

					// publish server as an OSGi service
					registration = publishServerAsAService(server);
					log.info("Published " + ServerInfo.getServerInfo() + " as an OSGi service");
				}
				catch (Exception ex) {
					String msg = "Cannot start " + ServerInfo.getServerInfo();
					log.error(msg, ex);
					throw new RuntimeException(msg, ex);
				}
				finally {
					current.setContextClassLoader(old);
				}
			}
		}, "Tomcat Catalina Start Thread");

		startupThread.start();
	}

	public void stop(BundleContext context) throws Exception {
		// unpublish service first
		registration.unregister();

		log.info("Unpublished  " + ServerInfo.getServerInfo() + " OSGi service");

		// default startup procedure
		ClassLoader cl = Activator.class.getClassLoader();
		Thread current = Thread.currentThread();
		ClassLoader old = current.getContextClassLoader();

		try {
			current.setContextClassLoader(cl);
			//reset CCL 
			// current.setContextClassLoader(null);
			log.info("Stopping " + ServerInfo.getServerInfo() + " ...");
			server.stop();
			log.info("Succesfully stopped " + ServerInfo.getServerInfo());
		}
		catch (Exception ex) {
			log.error("Cannot stop " + ServerInfo.getServerInfo(), ex);
			throw ex;
		}
		finally {
			current.setContextClassLoader(old);
		}
	}

	private Embedded createCatalinaServer(Configuration configuration) {
		// create embedded server
		Embedded embedded = new Embedded();
		embedded.setCatalinaHome(configuration.getHome());
		embedded.setName("Catalina");

		// add listener(s) (removed since it only works on 6.0.x+ )
		// embedded.addLifecycleListener(new JasperListener());

		// create engine
		Engine engine = embedded.createEngine();
		engine.setDefaultHost(configuration.getHost());
		engine.setName("Catalina");

		// engine -> server
		embedded.addEngine(engine);

		// create host
		StandardHost host = new StandardHost();
		host.setName(configuration.getHost());
		host.setDeployOnStartup(false);
		host.setLiveDeploy(false);
		host.setAutoDeploy(false);
		host.setXmlValidation(false);
		host.setXmlNamespaceAware(false);

		//host.setWorkDir(workDir);
		host.setUnpackWARs(false);

		// add the host -> engine
		engine.addChild(host);

		// create a plain HTTP server (no HTTPS)
		Connector http = embedded.createConnector(configuration.getHost(), configuration.getPort(), false);
		embedded.addConnector(http);

		// everything is configured, return the server
		return embedded;
	}

	private Configuration readConfiguration(Bundle bundle) throws IOException {
		// read default location
		Properties defaults = new Properties();

		URL defaultLocation = bundle.getResource(DEFAULT_CONF_LOCATION);
		if (defaultLocation == null)
			throw new IllegalArgumentException("Cannot find default location " + DEFAULT_CONF_LOCATION);

		loadStream(defaults, defaultLocation.openStream());

		// user properties
		Properties userProps;

		URL userConfigLocation = bundle.getResource(CONF_LOCATION);
		// check if indeed we have something
		if (userConfigLocation == null) {
			userProps = defaults;
			if (log.isDebugEnabled())
				log.debug("Reading default server configuration from  " + defaultLocation);
		}

		else {
			if (log.isDebugEnabled())
				log.debug("Reading user server configuration from  " + userConfigLocation);

			userProps = new Properties(defaults);
			loadStream(userProps, userConfigLocation.openStream());
		}

		return new Configuration(userProps);
	}

	private void loadStream(Properties props, InputStream stream) throws IOException {
		try {
			props.load(stream);
		}
		finally {
			try {
				if (stream != null)
					stream.close();
			}
			catch (IOException ex) {
				// ignore
			}
		}
	}

	private ServiceRegistration publishServerAsAService(Embedded server) {
		Properties props = new Properties();
		// put some extra properties to easily identify the service
		props.put(Constants.SERVICE_VENDOR, "Spring Dynamic Modules");
		props.put(Constants.SERVICE_DESCRIPTION, ServerInfo.getServerInfo());
		props.put(Constants.BUNDLE_VERSION, ServerInfo.getServerNumber());
		props.put(Constants.BUNDLE_NAME, bundleContext.getBundle().getSymbolicName());

		// spring-dm specific property
		props.put("org.springframework.osgi.bean.name", "tomcat-server");

		// publish just the interfaces and the major classes (server/handlerWrapper)
		String[] classes = new String[] { Embedded.class.getName(), StandardService.class.getName(),
			Service.class.getName(), MBeanRegistration.class.getName(), Lifecycle.class.getName() };

		return bundleContext.registerService(classes, server, props);
	}
}