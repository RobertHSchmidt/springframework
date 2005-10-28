package org.springframework.webflow.config.registry;

/**
 * A management interface for managing Flow definition registries at runtime.
 * Provides the ability to query the size and state of the registry, as well as
 * refresh registered Flow definitions at runtime.
 * <p>
 * Flow registries that implement this interface may be exposed for management
 * over the JMX protocol. The following is an example of using Spring's JMX
 * {@link org.springframework.jmx.export.MBeanExporter} to export a flow registry to an MBeanServer:
 * 
 * <pre>
 * 	&lt;!-- Creates the registry of flow definitions for this application --&gt;
 * 	&lt;bean name=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.config.registry.XmlFlowRegistryFactoryBean&quot;&gt;
 * 		&lt;property name=&quot;definitionLocations&quot; value=&quot;/WEB-INF/flow1.xml&quot;/&gt;
 * 	&lt;/bean&gt;
 * 
 * 	&lt;!-- Automatically exports the created flowRegistry as an MBean --&gt;
 * 	&lt;bean id=&quot;mbeanExporter&quot; class=&quot;org.springframework.jmx.export.MBeanExporter&quot;&gt;
 * 		&lt;property name=&quot;beans&quot;&gt;
 * 			&lt;map&gt;
 * 				&lt;entry key=&quot;spring-webflow:name=flowRegistry&quot; value-ref=&quot;flowLocator&quot;/&gt;
 * 			&lt;/map&gt;
 * 		&lt;/property&gt;
 * 		&lt;property name=&quot;assembler&quot;&gt;
 * 			&lt;bean class=&quot;org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler&quot;&gt;
 * 				&lt;property name=&quot;managedInterfaces&quot; value=&quot;org.springframework.webflow.config.registry.FlowRegistryMBean&quot;/&gt;
 * 			&lt;/bean&gt;
 * 		&lt;/property&gt;
 * 	&lt;/bean&gt;
 * </pre>
 * 
 * With the above configuration, you may then use any JMX client (such as 
 * Sun's jConsole which ships with JDK 1.5) to refresh flow definitions at 
 * runtime.
 * @author Keith Donald
 */
public interface FlowRegistryMBean {

	/**
	 * Returns the names of the flow definitions registered in this registry.
	 * @return the flow definition names
	 */
	public String[] getFlowDefinitionIds();

	/**
	 * Return the number of flow definitions registered in this registry.
	 * @return the flow definition count;
	 */
	public int getFlowDefinitionCount();

	/**
	 * Refresh this flow definition registry, reloading all Flow definitions
	 * from their externalized representations.
	 */
	public void refresh();

	/**
	 * Refresh the Flow definition in this registry with the <code>id</code>
	 * provided, reloading it from it's externalized representation.
	 * @param id the id of the flow definition to refresh.
	 * @throws IllegalArgumentException if a flow with the id provided is not 
	 * stored in this registry.
	 */
	public void refresh(String id) throws IllegalArgumentException;

}