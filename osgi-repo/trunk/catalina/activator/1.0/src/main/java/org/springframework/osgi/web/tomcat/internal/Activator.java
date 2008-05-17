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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.management.MBeanRegistration;

import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Server;
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
 * Simple activator for starting Apache Tomcat Catalina container inside OSGi
 * using Tomcat's XML configuration files.
 * 
 * <p/> This activator looks initially for a <code>conf/server.xml</code> file
 * falling back to <code>conf/default-server.xml</code>. This allows the
 * default configuration to be tweaked through fragments for example.
 * 
 * @author Costin Leau
 */
public class Activator implements BundleActivator {

	/** logger */
	private static final Log log = LogFactory.getLog(Activator.class);

	/**
	 * user-configurable config location
	 * 
	 * @deprecated will be removed in RC1
	 */
	private static final String CONF_LOCATION = "conf/embedded-server.properties";

	/**
	 * default config location *
	 * 
	 * @deprecated will be removed in RC1
	 */
	private static final String DEFAULT_CONF_LOCATION = "conf/embedded-server-defaults.properties";

	/** default XML configuration */
	private static final String DEFAULT_XML_CONF_LOCATION = "conf/default-server.xml";

	/** user-configurable XML configuration */
	private static final String XML_CONF_LOCATION = "conf/server.xml";

	private BundleContext bundleContext;

	private StandardService server;

	private ServiceRegistration registration, legacyRegistration;

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

					server = createCatalinaServer(bundleContext.getBundle());

					server.start();

					Connector[] connectors = server.findConnectors();
					for (int i = 0; i < connectors.length; i++) {
						Connector conn = connectors[i];
						log.info("Succesfully started " + ServerInfo.getServerInfo() + " @ " + conn.getDomain() + ":"
								+ conn.getPort());
					}

					// publish server as an OSGi service
					registration = publishServerAsAService(server);
					legacyRegistration = publishServerAsAServiceInLegacyMode(server);
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
		legacyRegistration.unregister();

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

	private StandardService createCatalinaServer(Bundle bundle) throws Exception {
		// first try to use the XML file
		URL xmlConfiguration = bundle.getResource(XML_CONF_LOCATION);

		// if there is no custom XML file, check the custom properties
		if (xmlConfiguration == null && bundle.getResource(CONF_LOCATION) != null) {
			Configuration config = readConfiguration(bundle);
			return createServerFromProperties(config);
		}

		if (xmlConfiguration != null) {
			log.info("Using custom XML configuration " + xmlConfiguration);
		}
		else {
			xmlConfiguration = bundle.getResource(DEFAULT_XML_CONF_LOCATION);
			if (xmlConfiguration == null)
				log.error("No XML configuration found; bailing out...");
			else
				log.info("Using default XML configuration " + xmlConfiguration);
		}

		return createServerFromXML(xmlConfiguration);
	}

	private StandardService createServerFromXML(URL xmlConfiguration) throws IOException {
		OsgiCatalina catalina = new OsgiCatalina();
		catalina.setAwait(false);
		catalina.setUseShutdownHook(false);
		catalina.setName("Catalina");
		catalina.setParentClassLoader(Thread.currentThread().getContextClassLoader());

		// copy the URL file to a local temporary file (since Catalina doesn't use URL unfortunately)
		File configTempFile = File.createTempFile("dm.catalina", ".cfg.xml");
		configTempFile.deleteOnExit();

		// copy URL to temporary file
		copyURLToFile(xmlConfiguration.openStream(), new FileOutputStream(configTempFile));
		log.debug("Copied configuration " + xmlConfiguration + " to temporary file " + configTempFile);

		catalina.setConfigFile(configTempFile.getAbsolutePath());

		catalina.load();

		Server server = catalina.getServer();

		return (StandardService) server.findServices()[0];
	}

	private void copyURLToFile(InputStream inStream, FileOutputStream outStream) {

		int bytesRead;
		byte[] buf = new byte[4096];
		try {
			while ((bytesRead = inStream.read(buf)) >= 0) {
				outStream.write(buf, 0, bytesRead);
			}
		}
		catch (IOException ex) {
			throw (RuntimeException) new IllegalStateException("Cannot copy URL to file").initCause(ex);
		}
		finally {
			try {
				inStream.close();
			}
			catch (IOException ignore) {
			}
			try {
				outStream.close();
			}
			catch (IOException ignore) {
			}
		}
	}

	/**
	 * 
	 * @param configuration
	 * @return
	 * @deprecated will be removed in RC1
	 */
	private StandardService createServerFromProperties(Configuration configuration) {
		// create embedded server
		Embedded embedded = new Embedded();
		embedded.setCatalinaHome(configuration.getHome());
		embedded.setName("Catalina");

		// add listener(s) (removed since it only works on 6.0.x+ )
		// embedded.addLifecycleListener(new JasperListener());

		String hostName = configuration.getHost();
		// create host
		StandardHost host = new StandardHost();
		host.setName(hostName);
		host.setDeployOnStartup(false);
		host.setLiveDeploy(false);
		host.setAutoDeploy(false);
		host.setXmlValidation(false);
		host.setXmlNamespaceAware(false);

		//host.setWorkDir(workDir);
		host.setUnpackWARs(false);

		// create engine
		Engine engine = embedded.createEngine();
		engine.setDefaultHost(hostName);
		engine.setName("Catalina");

		// add the host -> engine
		engine.addChild(host);

		// engine -> server
		embedded.addEngine(engine);

		// create a plain HTTP server (no HTTPS)

		Connector http = embedded.createConnector((hostName.length() < 1 ? null : hostName), configuration.getPort(),
			false);
		http.setEnableLookups(false);

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

	private ServiceRegistration publishServerAsAService(StandardService server) {
		Properties props = new Properties();
		// put some extra properties to easily identify the service
		props.put(Constants.SERVICE_VENDOR, "Spring Dynamic Modules");
		props.put(Constants.SERVICE_DESCRIPTION, ServerInfo.getServerInfo());
		props.put(Constants.BUNDLE_VERSION, ServerInfo.getServerNumber());
		props.put(Constants.BUNDLE_NAME, bundleContext.getBundle().getSymbolicName());

		// spring-dm specific property
		props.put("org.springframework.osgi.bean.name", "tomcat-server");

		// publish just the interfaces and the major classes (server/handlerWrapper)
		String[] classes = new String[] { StandardService.class.getName(), Service.class.getName(),
			MBeanRegistration.class.getName(), Lifecycle.class.getName() };

		return bundleContext.registerService(classes, server, props);
	}

	/**
	 * Service publication for M1 builds. Will create an Embedded instance used
	 * just for publication.
	 * 
	 * @param server
	 * @return
	 * @deprecated will be removed in RC1
	 */
	private ServiceRegistration publishServerAsAServiceInLegacyMode(StandardService server) {

		Properties props = new Properties();
		// put some extra properties to easily identify the service
		props.put(Constants.SERVICE_VENDOR, "Spring Dynamic Modules");
		props.put(Constants.SERVICE_DESCRIPTION, ServerInfo.getServerInfo() + " deprecated");
		props.put(Constants.BUNDLE_VERSION, ServerInfo.getServerNumber());
		props.put(Constants.BUNDLE_NAME, bundleContext.getBundle().getSymbolicName());

		// spring-dm specific property
		props.put("org.springframework.osgi.bean.name", "tomcat-server");

		// publish just the interfaces and the major classes (server/handlerWrapper)
		String[] classes = new String[] { Embedded.class.getName() };

		return bundleContext.registerService(classes, createEmbeddedServerFromServer(server), props);
	}

	private Embedded createEmbeddedServerFromServer(StandardService server) {
		Embedded embedded = new Embedded();
		embedded.setName("Catalina");
		embedded.addEngine((Engine) server.getContainer());

		for (int connectorsIndex = 0; connectorsIndex < server.findConnectors().length; connectorsIndex++) {
			Connector connector = server.findConnectors()[connectorsIndex];
			embedded.addConnector(connector);
		}

		return embedded;
	}
}