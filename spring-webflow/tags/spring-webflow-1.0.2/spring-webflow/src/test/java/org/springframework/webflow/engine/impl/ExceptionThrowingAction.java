/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.webflow.engine.impl;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Test action that throws an exception.
 * 
 * @author Erwin Vervaet
 */
public class ExceptionThrowingAction extends AbstractAction {

	protected Event doExecute(RequestContext context) throws Exception {
		Class exceptionType = Class.forName(context.getAttributes().getString("exceptionType"));
		throw (Exception)exceptionType.newInstance();
	}

}
