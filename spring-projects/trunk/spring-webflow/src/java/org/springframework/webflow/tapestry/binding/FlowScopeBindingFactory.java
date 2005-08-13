package org.springframework.webflow.tapestry.binding;

import org.apache.hivemind.Location;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.binding.AbstractBindingFactory;

/**
 * Binding factory that produces FlowScopeBindings.
 * 
 * @see org.springframework.webflow.tapestry.binding.FlowScopeBinding
 * 
 * @author Keith Donald
 */
public class FlowScopeBindingFactory extends AbstractBindingFactory {
	
	public IBinding createBinding(IComponent root, String bindingDescription, String path, Location location) {
		return new FlowScopeBinding(root.getPage(), path, bindingDescription,
				getValueConverter(), location);
	}
}
