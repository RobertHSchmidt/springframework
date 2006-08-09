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
 * A single flow definition.
 * <p>
 * Exposes the flow's identifier, states, and attributes.
 * 
 * @author Keith Donald
 */
public interface FlowDefinition extends Annotated {

	/**
	 * Returns the unique id of this flow.
	 * @return the flow id
	 */
	public String getId();

	/**
	 * Return this flow's starting point.
	 * @return the start state
	 */
	public StateDefinition getStartState();	
	
	/**
	 * Returns the state definition with the specified id.
	 * @param id the state id
	 * @return the state definition
	 * @throws IllegalArgumentException if a state with this id does not exist
	 */
	public StateDefinition getState(String id) throws IllegalArgumentException;
}