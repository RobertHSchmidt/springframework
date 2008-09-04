/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.core.launch.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ApplicationContextJobFactory;
import org.springframework.batch.core.configuration.support.ClassPathXmlApplicationContextFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>
 * Command line launcher for registering jobs with a {@link JobRegistry}.
 * Normally this will be used in conjunction with an external trigger for the
 * jobs registered, e.g. a JMX MBean wrapper for a {@link JobLauncher}, or a
 * Quartz trigger.
 * </p>
 * 
 * <p>
 * With any launch of a batch job within Spring Batch, a Spring context
 * containing the {@link Job} has to be created. Using this launcher, the jobs
 * are all registered with a {@link JobRegistry} defined in a parent application
 * context. The jobs are then set up in child contexts. All dependencies of the
 * runner will then be satisfied by autowiring by type from the parent
 * application context. Default values are provided for all fields except the
 * {@link JobRegistry}. Therefore, if autowiring fails to set it then an
 * exception will be thrown.
 * </p>
 * 
 * @author Dave Syer
 * 
 */
public class JobRegistryBackgroundJobRunner {

	/**
	 * System property key that switches the runner to "embedded" mode
	 * (returning immediately from the main method). Useful for testing
	 * purposes.
	 */
	public static final String EMBEDDED = JobRegistryBackgroundJobRunner.class.getSimpleName() + ".EMBEDDED";

	private static Log logger = LogFactory.getLog(JobRegistryBackgroundJobRunner.class);

	private JobRegistry registry;

	private ApplicationContext parentContext = null;

	final private String parentContextPath;

	private static List<RuntimeException> errors = new ArrayList<RuntimeException>();

	/**
	 * @param parentContextPath
	 */
	public JobRegistryBackgroundJobRunner(String parentContextPath) {
		super();
		this.parentContextPath = parentContextPath;
	}

	/**
	 * Public setter for the {@link JobRegistry}.
	 * @param registry the registry to set
	 */
	public void setRegistry(JobRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Public getter for the startup errors encountered during parent context
	 * creation.
	 * @return the errors
	 */
	public static List<RuntimeException> getErrors() {
		return errors;
	}

	private void register(String[] paths) throws DuplicateJobException, IOException {

		for (int i = 0; i < paths.length; i++) {

			Resource[] resources = parentContext.getResources(paths[i]);

			for (int j = 0; j < resources.length; j++) {

				Resource path = resources[j];
				logger.info("Registering Job definitions from " + resources);

				ConfigurableListableBeanFactory beanFactory = new XmlBeanFactory(path, parentContext
						.getAutowireCapableBeanFactory());
				String[] names = beanFactory.getBeanNamesForType(Job.class);

				for (int k = 0; k < names.length; k++) {
					ClassPathXmlApplicationContextFactory factory = new ClassPathXmlApplicationContextFactory();
					factory.setApplicationContext(parentContext);
					factory.setPath(path);
					logger.info("Registering Job definition: " + names[k]);
					registry.register(new ApplicationContextJobFactory(factory, names[k]));
				}

			}

		}

	}

	/**
	 * Supply a list of application context locations, starting with the parent
	 * context, and followed by the children. The parent must contain a
	 * {@link JobRegistry} and the child contexts are expected to contain
	 * {@link Job} definitions, each of which will be registered wit the
	 * registry.
	 * 
	 * Example usage:
	 * 
	 * <pre>
	 * $ java -classpath ... JobRegistryBackgroundJobRunner job-registry-context.xml job1.xml job2.xml ...
	 * </pre>
	 * 
	 * The child contexts are created only when needed though the
	 * {@link JobFactory} interface (but the XML is validated on startup by
	 * using it to create a {@link BeanFactory} which is then discarded).
	 * 
	 * The parent context is created in a separate thread, and the program will
	 * pause for input in an infinite loop until the user hits any key.
	 * 
	 * @param args the context locations to use (first one is for parent)
	 * @throws Exception if anything goes wrong with the context creation
	 */
	public static void main(String... args) throws Exception {

		Assert.state(args.length >= 1, "At least one argument (the parent context path) must be provided.");

		final JobRegistryBackgroundJobRunner launcher = new JobRegistryBackgroundJobRunner(args[0]);
		errors.clear();

		logger.info("Starting job registry in parent context from XML at: [" + args[0] + "]");

		new Thread(new Runnable() {
			public void run() {
				try {
					launcher.run();
				}
				catch (RuntimeException e) {
					errors.add(e);
					throw e;
				}
			};
		}).start();

		logger.info("Waiting for parent context to start.");
		while (launcher.parentContext == null && errors.isEmpty()) {
			Thread.sleep(100L);
		}
		if (!errors.isEmpty()) {
			logger.info(errors.size() + " errors detected on startup of parent context.  Rethrowing.");
			throw errors.get(0);
		}

		// Paths to individual job configurations.
		final String[] paths = new String[args.length - 1];
		System.arraycopy(args, 1, paths, 0, paths.length);

		logger.info("Parent context started.  Registering jobs from paths: " + Arrays.asList(paths));
		launcher.register(paths);

		if (System.getProperty(EMBEDDED) != null) {
			return;
		}

		System.out.println("Started application.  Hit any key to exit.");
		System.in.read();

	}

	private void run() {
		final ApplicationContext parent = new ClassPathXmlApplicationContext(parentContextPath);
		parent.getAutowireCapableBeanFactory().autowireBeanProperties(this,
				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		parent.getAutowireCapableBeanFactory().initializeBean(this, getClass().getSimpleName());
		this.parentContext = parent;
	}

}
