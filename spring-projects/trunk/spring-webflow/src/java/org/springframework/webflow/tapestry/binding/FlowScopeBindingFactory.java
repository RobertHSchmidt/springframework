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
