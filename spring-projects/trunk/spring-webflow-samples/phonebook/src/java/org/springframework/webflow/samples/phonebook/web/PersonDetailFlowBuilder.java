/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.webflow.samples.phonebook.web;

import org.springframework.binding.support.Mapping;
import org.springframework.webflow.Transition;
import org.springframework.webflow.builder.AbstractFlowBuilder;
import org.springframework.webflow.builder.FlowBuilderException;
import org.springframework.webflow.support.ParameterizableFlowAttributeMapper;

/**
 * Java-based flow builder that builds the person details flow, exactly like it
 * is defined in the <code>detail.xml</code> XML flow definition.
 * <p>
 * This encapsulates the page flow of viewing a person's details and their
 * collegues in a reusable, self-contained module.
 * 
 * @author Keith Donald
 */
public class PersonDetailFlowBuilder extends AbstractFlowBuilder {

	private static final String GET_PERSON = "getPerson";

	private static final String DISPLAY_DETAILS = "displayDetails";

	private static final String BROWSE_COLLEAGUE_DETAILS = "browseColleagueDetails";

	public void buildStates() throws FlowBuilderException {
		// get the person given a userid as input
		addActionState(GET_PERSON, method("getPerson(${flowScope.id})", action("phonebook")), on(success(),
				DISPLAY_DETAILS));

		// view the person details
		addViewState(DISPLAY_DETAILS, "details", new Transition[] { on(back(), "finish"),
				on(select(), BROWSE_COLLEAGUE_DETAILS) });

		// view details for selected collegue
		ParameterizableFlowAttributeMapper idMapper = new ParameterizableFlowAttributeMapper();
		idMapper.setInputMapping(new Mapping("sourceEvent.parameters.id", "id", fromStringTo(Long.class)));
		addSubflowState(BROWSE_COLLEAGUE_DETAILS, flow("detail"), idMapper,
				new Transition[] { on(finish(), GET_PERSON) });

		// end
		addEndState("finish");

		// end error
		addEndState("error");
	}
}