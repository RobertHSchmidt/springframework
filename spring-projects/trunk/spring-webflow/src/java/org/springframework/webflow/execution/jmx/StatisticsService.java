/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.webflow.execution.jmx;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.FlowExecutionManager;

/**
 * Managed service that collects statistics on the web flow system.
 * 
 * @author Keith Donald
 */
public class StatisticsService implements InitializingBean, DisposableBean {

	/**
	 * A JMX key attribute "serviceType".
	 */
	private static final String SERVICE_TYPE_KEY_NAME = "serviceType";

	/**
	 * The default "spring-webflow" JMX domain name.
	 */
	private static final String SPRING_WEBFLOW_DOMAIN = "spring-webflow";

	/**
	 * The execution manager to collect statistics on.
	 */
	private FlowExecutionManager flowExecutionManager;

	/**
	 * A global statistics bean (stats holder). 
	 */
	private GlobalStatistics globalStats = new GlobalStatistics();

	/**
	 * The statistics collector, a flow execution listener. 
	 */
	private FlowExecutionListener statisticsCollector = new StatisticsCollector();

	/**
	 * The MBeanServer the statistics MBean should be registered in.
	 */
	private MBeanServer mbeanServer;

	/**
	 * The ObjectName of the global statistics MBean. 
	 */
	private ObjectName globalStatsMBeanName;

	/**
	 * Creates a Web Flow statistics service that will collect stats on FlowExecutions managed by
	 * the provided execution manger.
	 * @param flowExecutionManager the flow execution manager
	 */
	public StatisticsService(FlowExecutionManager flowExecutionManager) {
		this.flowExecutionManager = flowExecutionManager;
	}

	/**
	 * @param mbeanServer
	 */
	public void setMBeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	/**
	 * Set if stats collection should be enabled.
	 */
	public void setEnabled(boolean enabled) {
		this.globalStats.setStatisticsEnabled(enabled);
	}

	public void afterPropertiesSet() throws Exception {
		// register global stats mbean
		if (mbeanServer == null) {
			List servers = MBeanServerFactory.findMBeanServer(null);
			mbeanServer = (MBeanServer)servers.get(0);
		}
		globalStatsMBeanName = new ObjectName(SPRING_WEBFLOW_DOMAIN, SERVICE_TYPE_KEY_NAME, "globalStatistics");
		mbeanServer.registerMBean(globalStats, globalStatsMBeanName);
		this.flowExecutionManager.addListener(statisticsCollector);
	}

	public void destroy() throws Exception {
		mbeanServer.unregisterMBean(globalStatsMBeanName);
		this.flowExecutionManager.removeListener(statisticsCollector);
	}

	/**
	 * Flow execution listener that collects statistics about the web flow
	 * system.
	 * 
	 * @author Keith Donald
	 */
	protected final class StatisticsCollector extends FlowExecutionListenerAdapter {

		protected boolean statisticsEnabled(RequestContext context) {
			return globalStats.statisticsEnabled;
		}

		public void requestSubmitted(RequestContext context) {
			if (statisticsEnabled(context)) {
				globalStats.totalRequestCount++;
				globalStats.requestsInProcessCount++;
			}
		}

		public void sessionStarted(RequestContext context) {
			if (statisticsEnabled(context)) {
				if (context.getFlowExecutionContext().isRootFlowActive()) {
					globalStats.totalFlowExecutionCount++;
					globalStats.managedFlowExecutionCount++;
				}
				globalStats.activeFlowExecutionCount++;
			}
		}
		
		public void paused(RequestContext context, ViewDescriptor selectedView) {
			if (statisticsEnabled(context)) {
				globalStats.pausedFlowExecutionCount++;
				globalStats.activeFlowExecutionCount--;
			}
		}

		public void resumed(RequestContext context) {
			if (statisticsEnabled(context)) {
				globalStats.pausedFlowExecutionCount--;
				globalStats.activeFlowExecutionCount++;
			}
		}

		public void sessionEnded(RequestContext context, FlowSession endedSession) {
			if (statisticsEnabled(context)) {
				if (endedSession.isRoot()) {
					globalStats.endedFlowExecutionCount++;
					globalStats.managedFlowExecutionCount--;
					globalStats.activeFlowExecutionCount--;
				}
			}
		}

		public void requestProcessed(RequestContext context) {
			if (statisticsEnabled(context)) {
				globalStats.requestsInProcessCount--;
			}
		}
	}
}