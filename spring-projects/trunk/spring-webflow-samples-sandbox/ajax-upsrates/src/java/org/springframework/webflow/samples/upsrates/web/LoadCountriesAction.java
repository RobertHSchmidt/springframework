/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.webflow.samples.upsrates.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

/**
 * Load sender countries in request scope. This is a stub implementation that doesn't
 * load reference data from a database.
 * 
 * 
 * @author Steven Devijver
 * @since Sep 25, 2005
 */
public class LoadCountriesAction extends AbstractAction {

	protected Event doExecute(RequestContext context) throws Exception {

		Map countries = new HashMap();
		countries.put("US", "United States");
		countries.put("CA", "Canada");
		context.getRequestScope().setAttribute("countries", countries);
		
		return success();
	}
}
