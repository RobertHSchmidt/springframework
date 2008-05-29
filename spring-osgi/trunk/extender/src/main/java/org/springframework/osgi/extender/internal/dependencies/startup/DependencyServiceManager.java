
package org.springframework.osgi.extender.internal.dependencies.startup;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.extender.event.BootstrappingDependencyEvent;
import org.springframework.osgi.service.importer.OsgiServiceImportDependencyDefinition;
import org.springframework.osgi.service.importer.OsgiServiceImportDependencyFactory;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencySatisfiedEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyTimedOutEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitingEvent;
import org.springframework.osgi.service.importer.support.AbstractOsgiServiceImportFactoryBean;
import org.springframework.osgi.util.OsgiListenerUtils;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * ServiceListener used for tracking dependent services. Even if the
 * ServiceListener receives event synchronously, mutable properties should be
 * synchronized to guarantee safe publishing between threads.
 * 
 * @author Costin Leau
 * @author Hal Hildebrand
 * @author Andy Piper
 */
public class DependencyServiceManager {

	private static final Log log = LogFactory.getLog(DependencyServiceManager.class);

	protected final Map dependencies = Collections.synchronizedMap(new LinkedHashMap());

	protected final Map unsatisfiedDependencies = Collections.synchronizedMap(new LinkedHashMap());

	private final ContextExecutorStateAccessor contextStateAccessor;

	private final BundleContext bundleContext;

	private final ServiceListener listener;

	private final DelegatedExecutionOsgiBundleApplicationContext context;

	/**
	 * Task to execute if all dependencies are met.
	 */
	private final Runnable executeIfDone;

	/** Maximum waiting time used in events when waiting for dependencies */
	private final long waitTime;


	/**
	 * Actual ServiceListener.
	 * 
	 * @author Costin Leau
	 * @author Hal Hildebrand
	 */
	private class DependencyServiceListener implements ServiceListener {

		/**
		 * Process serviceChanged events, completing context initialization if
		 * all the required dependencies are satisfied.
		 * 
		 * @param serviceEvent
		 */
		public void serviceChanged(ServiceEvent serviceEvent) {
			boolean trace = log.isTraceEnabled();

			try {
				if (unsatisfiedDependencies.isEmpty()) {

					// already completed but likely called due to threading
					if (trace) {
						log.trace("Handling service event, but no unsatisfied dependencies exist for "
								+ context.getDisplayName());
					}

					return;
				}

				ServiceReference ref = serviceEvent.getServiceReference();
				if (trace) {
					log.trace("Handling service event [" + OsgiStringUtils.nullSafeToString(serviceEvent) + ":"
							+ OsgiStringUtils.nullSafeToString(ref) + "] for " + context.getDisplayName());
				}

				updateDependencies(serviceEvent);

				ContextState state = contextStateAccessor.getContextState();

				// already resolved (closed or timed-out)
				if (state.isResolved()) {
					deregister();
					return;
				}

				// Good to go!
				if (unsatisfiedDependencies.isEmpty()) {
					deregister();
					// context.listener = null;
					log.info("No outstanding OSGi service dependencies, completing initialization for "
							+ context.getDisplayName());

					// execute task to complete initialization
					// NOTE: the runnable should be able to delegate any long
					// process to a
					// different thread.
					executeIfDone.run();
				}
			}
			catch (Throwable e) {
				// frameworks will simply not log exception for event handlers
				log.error("Exception during dependency processing for " + context.getDisplayName(), e);
			}
		}

		private void updateDependencies(ServiceEvent serviceEvent) {
			boolean trace = log.isTraceEnabled();
			boolean debug = log.isDebugEnabled();

			for (Iterator i = dependencies.keySet().iterator(); i.hasNext();) {
				ServiceDependency dependency = (ServiceDependency) i.next();

				// check if there is a match on the service
				if (dependency.matches(serviceEvent)) {
					switch (serviceEvent.getType()) {

						case ServiceEvent.REGISTERED:
						case ServiceEvent.MODIFIED:
							unsatisfiedDependencies.remove(dependency);
							if (debug) {
								log.debug("Found service for " + context.getDisplayName() + "; eliminating "
										+ dependency + ", remaining [" + unsatisfiedDependencies + "]");
							}

							sendDependencySatisfiedEvent(dependency);
							break;

						case ServiceEvent.UNREGISTERING:
							unsatisfiedDependencies.put(dependency, dependency.getBeanName());
							if (debug) {
								log.debug("Service unregistered; adding " + dependency);
							}
							sendDependencyUnsatisfiedEvent(dependency);
							break;
						default: // do nothing
							if (debug) {
								log.debug("Unknown service event type for: " + dependency);
							}
							break;
					}
				}
				else {
					if (trace) {
						log.trace(dependency + " does not match: "
								+ OsgiStringUtils.nullSafeToString(serviceEvent.getServiceReference()));
					}
				}
			}
		}

	}


	/**
	 * Create a dependency manager, indicating the executor bound to, the
	 * context that contains the dependencies and the task to execute if all
	 * dependencies are met.
	 * 
	 * @param executor
	 * @param context
	 * @param executeIfDone
	 */
	public DependencyServiceManager(ContextExecutorStateAccessor executor,
			DelegatedExecutionOsgiBundleApplicationContext context, Runnable executeIfDone, long maxWaitTime) {
		this.contextStateAccessor = executor;
		this.context = context;
		this.waitTime = maxWaitTime;
		this.bundleContext = context.getBundleContext();
		this.listener = new DependencyServiceListener();

		this.executeIfDone = executeIfDone;
	}

	protected void findServiceDependencies() {
		Thread currentThread = Thread.currentThread();
		ClassLoader oldTCCL = currentThread.getContextClassLoader();

		boolean debug = log.isDebugEnabled();
		try {
			currentThread.setContextClassLoader(context.getClassLoader());

			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			String[] beans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory,
				AbstractOsgiServiceImportFactoryBean.class, true, false);
			for (int i = 0; i < beans.length; i++) {
				String beanName = (beans[i].startsWith(BeanFactory.FACTORY_BEAN_PREFIX) ? beans[i]
						: BeanFactory.FACTORY_BEAN_PREFIX + beans[i]);

				AbstractOsgiServiceImportFactoryBean reference = (AbstractOsgiServiceImportFactoryBean) beanFactory.getBean(beanName);
				ServiceDependency dependency = new ServiceDependency(bundleContext, reference.getUnifiedFilter(),
					reference.isMandatory(), beanName);

				dependencies.put(dependency, dependency.getBeanName());
				if (!dependency.isServicePresent()) {
					log.info("Adding OSGi service dependency for importer " + beanName);
					unsatisfiedDependencies.put(dependency, dependency.getBeanName());
				}
			}
			// Add dependencies defined by any OsgiServiceImportDependencyFactorys.
			beans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory,
				OsgiServiceImportDependencyFactory.class, true, false);
			for (int i = 0; i < beans.length; i++) {
				String beanName = beans[i];
				OsgiServiceImportDependencyFactory reference = (OsgiServiceImportDependencyFactory) beanFactory.getBean(beanName);
				Set depList = reference.getServiceDependencyDefinitions();
				for (Iterator iter = depList.iterator(); iter.hasNext();) {
					OsgiServiceImportDependencyDefinition def = (OsgiServiceImportDependencyDefinition) iter.next();
					ServiceDependency dependency = new ServiceDependency(bundleContext, def.getFilter(),
						def.isMandatory(), def.getBeanName());

					dependencies.put(dependency, dependency.getBeanName());
					if (!dependency.isServicePresent()) {
						if (debug)
							log.debug("adding OSGi service dependency for filter " + def.getFilter());
						unsatisfiedDependencies.put(dependency, dependency.getBeanName());
					}
				}
			}
		}
		finally {
			currentThread.setContextClassLoader(oldTCCL);
		}

		if (debug) {
			log.debug(dependencies.size() + " OSGi service dependencies, " + unsatisfiedDependencies.size()
					+ " unsatisfied (for beans " + unsatisfiedDependencies.values() + ") in "
					+ context.getDisplayName());
		}
		if (log.isTraceEnabled()) {
			log.trace("Total OSGi service dependencies beans " + dependencies.values());
			log.trace("Unsatified OSGi service dependencies beans " + unsatisfiedDependencies.values());
		}
	}

	protected boolean isSatisfied() {
		return unsatisfiedDependencies.isEmpty();
	}

	public Map getUnsatisfiedDependencies() {
		return unsatisfiedDependencies;
	}

	protected void register() {
		String filter = createDependencyFilter();
		if (log.isDebugEnabled()) {
			log.debug(context.getDisplayName() + " has registered service dependency dependencyDetector with filter: "
					+ filter);
		}

		// send dependency event before registering the filter
		sendInitialDependencyEvents();
		OsgiListenerUtils.addServiceListener(bundleContext, listener, filter);
	}

	/**
	 * Look at the existing dependencies and create an appropriate filter. This
	 * method concatenates the filters into one.
	 * 
	 * @return
	 */
	protected String createDependencyFilter() {
		boolean multiple = unsatisfiedDependencies.size() > 1;
		StringBuffer sb = new StringBuffer(100 * unsatisfiedDependencies.size());
		if (multiple) {
			sb.append("(|");
		}
		for (Iterator i = unsatisfiedDependencies.keySet().iterator(); i.hasNext();) {
			sb.append(((ServiceDependency) i.next()).filterAsString);
		}
		if (multiple) {
			sb.append(')');
		}
		return sb.toString();
	}

	protected void deregister() {
		if (log.isDebugEnabled()) {
			log.debug("Deregistering service dependency dependencyDetector for " + context.getDisplayName());
		}

		OsgiListenerUtils.removeServiceListener(bundleContext, listener);
	}

	// event notification
	private void sendInitialDependencyEvents() {
		for (Iterator iterator = unsatisfiedDependencies.keySet().iterator(); iterator.hasNext();) {
			ServiceDependency entry = (ServiceDependency) iterator.next();
			OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencyWaitingEvent(context,
				entry.getServiceDependency(), waitTime);
			BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context, nestedEvent);
			publishEvent(dependencyEvent);
		}
	}

	private void sendDependencyUnsatisfiedEvent(ServiceDependency dependency) {
		OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencyWaitingEvent(context,
			dependency.getServiceDependency(), waitTime);
		BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context, nestedEvent);
		publishEvent(dependencyEvent);
	}

	private void sendDependencySatisfiedEvent(ServiceDependency dependency) {
		OsgiServiceDependencyEvent nestedEvent = new OsgiServiceDependencySatisfiedEvent(context,
			dependency.getServiceDependency(), waitTime);
		BootstrappingDependencyEvent dependencyEvent = new BootstrappingDependencyEvent(context, nestedEvent);
		publishEvent(dependencyEvent);
	}

	private void publishEvent(BootstrappingDependencyEvent dependencyEvent) {
		this.contextStateAccessor.getEventMulticaster().multicastEvent(dependencyEvent);
	}
}
