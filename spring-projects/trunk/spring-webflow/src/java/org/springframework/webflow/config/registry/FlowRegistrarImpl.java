package org.springframework.webflow.config.registry;

import org.springframework.core.style.ToStringCreator;
import org.springframework.webflow.config.FlowBuilder;

/**
 * A default flow registrar implementation that registers the flow definitions
 * built by a configured set of Flow builders. This class is useful in
 * conjunction with use of custom FlowBuilder implementations.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * public class MyFlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {
 *     public void registerFlowDefinitions(FlowRegistry registry) {
 *         FlowBuilder[] builders = new FlowBuilder[] {
 *             new AbstractFlowBuilder() {
 * 			       protected String flowId() {
 * 				       return &quot;flowId1&quot;;
 * 			       }
 * 
 * 			       public void buildStates() throws FlowBuilderException {
 *                     // add states here
 * 			       }
 *             },
 * 		       new AbstractFlowBuilder() {
 * 			       protected String flowId() {
 * 				       return &quot;flowId2&quot;;
 * 			       }
 * 
 * 			       public void buildStates() throws FlowBuilderException {
 * 				       // add states here
 * 			       }
 * 		       };
 * 	       new FlowRegistrarImpl(builders).registerFlowDefinitions(registry);
 *     }
 * }
 * </pre>
 * 
 * @author Keith Donald
 */
public class FlowRegistrarImpl implements FlowRegistrar {

	/**
	 * The builders that will build the Flow definitions to register.
	 */
	private FlowBuilder[] flowBuilders;

	/**
	 * Creates a Flow registrar registering Flows built by the provided builder
	 * list.
	 */
	public FlowRegistrarImpl(FlowBuilder[] flowBuilders) {
		this.flowBuilders = flowBuilders;
	}

	public void registerFlowDefinitions(FlowRegistry registry) {
		if (flowBuilders != null) {
			for (int i = 0; i < flowBuilders.length; i++) {
				registry.registerFlowDefinition(new FlowAssembler(flowBuilders[i]));
			}
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("flowBuilders", flowBuilders).toString();
	}
}