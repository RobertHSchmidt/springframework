/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.webflow.definition;

/**
 * A transition takes a flow from one state to another. A transition is
 * typically triggered by an event.
 * 
 * @author Keith Donald
 */
public interface TransitionDefinition extends Annotated {

	/**
	 * The identifier of this transition. This id value should be unique among
	 * all other transitions in a set.
	 * @return the transition identifier
	 */
	public String getId();

	/**
	 * Returns the target state of this transition.
	 * @return the target state
	 */
	public String getTargetStateId();
}