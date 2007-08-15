/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.batch.sample.dao;

import org.springframework.batch.item.ResourceLifecycle;
import org.springframework.batch.sample.domain.CustomerCredit;

/**
 * Interface for writing customer's credit information to output.
 * 
 * @author Robert Kasanicky
 */
public interface CustomerCreditWriter extends ResourceLifecycle{

	void write(CustomerCredit customerCredit);
	
}
