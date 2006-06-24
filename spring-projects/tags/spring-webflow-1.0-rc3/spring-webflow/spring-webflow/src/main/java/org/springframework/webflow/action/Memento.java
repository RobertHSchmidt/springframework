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

import java.io.Serializable;

/**
 * An abstract base class for mementos that encapsulate the state of a
 * {@link MementoOriginator}. Basically, a token storing the state of another
 * object.
 * <p>
 * Mementos are expected to be managed by caretakers (clients) without
 * the clents being aware of their internal structure. Only the originator is aware of
 * the internal structure of a concrete Memento implementation.
 * <p>
 * Memento is an abstract class because it's expected implementations are concrete
 * mementos and not part of some other inheritence hierarchy. Mementos are
 * serializable snapshots of data.
 * 
 * @see org.springframework.webflow.action.MementoOriginator
 * 
 * @author Keith Donald
 */
public abstract class Memento implements Serializable {

}
