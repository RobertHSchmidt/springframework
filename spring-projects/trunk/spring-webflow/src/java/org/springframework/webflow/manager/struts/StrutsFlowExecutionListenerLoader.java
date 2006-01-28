package org.springframework.webflow.manager.struts;

import org.springframework.web.struts.SpringBindingActionForm;
import org.springframework.webflow.manager.ConditionalFlowExecutionListenerLoader;

/**
 * Simple extension of {@link ConditionalFlowExecutionListenerLoader} that adds
 * a {@link SpringBindingActionFormConfigurer} listener that applies to all flows.
 * <p>
 * Note: this adapter is required when using Struts and Spring Web Flow together with
 * Spring's {@link SpringBindingActionForm} to take advantage of Spring's
 * POJO-based data binding capabilities within a Struts environment.
 * 
 * @author Keith Donald
 */
public class StrutsFlowExecutionListenerLoader extends ConditionalFlowExecutionListenerLoader {
	
	/**
	 * Creates a new struts listener loader that simply attaches a {@link SpringBindingActionFormConfigurer}.
	 */
	public StrutsFlowExecutionListenerLoader() {
		addListener(new SpringBindingActionFormConfigurer());
	}
}
