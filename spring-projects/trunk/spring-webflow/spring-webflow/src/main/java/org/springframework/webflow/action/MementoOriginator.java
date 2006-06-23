/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.action;

/**
 * An object that is the originator of state that can be captured in a memento
 * token. Typically the object will capture its <i>own</i> state in the memento.
 * <p>
 * Provides an operation for {@link #createMemento() creating a token} capturing
 * state, as well as an operation for
 * {@link #setMemento(Memento) setting a token} to update originator state.
 * 
 * @author Keith Donald
 */
public interface MementoOriginator {

	/**
	 * Create a memento holding the state of this originator.
	 * @return the memento
	 */
	public Memento createMemento();

	/**
	 * Update the state of the originator based on the memento.
	 * @param memento the memento
	 */
	public void setMemento(Memento memento);
}