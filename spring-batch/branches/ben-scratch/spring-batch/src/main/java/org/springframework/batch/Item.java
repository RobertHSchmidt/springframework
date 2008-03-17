/*
 * Copyright 2002-2008 the original author or authors.
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
package org.springframework.batch;

import java.io.Serializable;

/**
 * An item to be processed.
 * 
 * @author Ben Hale
 */
public interface Item {

	/**
	 * Returns a correlation id for this item. This identifier need not be globally unique. This
	 * identifier must be {@link Serializable}.
	 * 
	 * @return the correlation id for this items
	 */
	Object getId();
}
