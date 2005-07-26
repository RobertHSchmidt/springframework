/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.samples.phonebook.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.support.Assert;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.test.AbstractFlowExecutionTests;

public class SearchPersonFlowTests extends AbstractFlowExecutionTests {

	public SearchPersonFlowTests() {
		setDependencyCheck(false);
	}

	protected String flowId() {
		return "searchFlow";
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/webflow/samples/phonebook/deploy/service-layer.xml",
				"classpath:org/springframework/webflow/samples/phonebook/deploy/web-layer.xml" };
	}

	public void testStartFlow() {
		startFlow();
		assertCurrentStateEquals("displayCriteria");
	}
	
	public void testCriteriaView_Submit_Success() {
		startFlow();
		Map parameters = new HashMap();
		parameters.put("firstName", "Keith");
		parameters.put("lastName", "Donald");
		ViewDescriptor view = signalEvent(event("search", parameters));
		assertCurrentStateEquals("displayResults");
		Assert.collectionAttributeSizeEquals(view, "persons", 1);
	}
	
	public void testCriteriaView_Submit_Error() {
		startFlow();
		// simulate user error by not passing in any params
		signalEvent(event("search"));
		assertCurrentStateEquals("displayCriteria");
	}

}