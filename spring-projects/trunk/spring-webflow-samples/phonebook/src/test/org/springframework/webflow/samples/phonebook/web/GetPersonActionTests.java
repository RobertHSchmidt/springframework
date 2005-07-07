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

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.binding.support.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.samples.phonebook.domain.Person;
import org.springframework.webflow.samples.phonebook.domain.PhoneBook;
import org.springframework.webflow.samples.phonebook.web.GetPersonAction;
import org.springframework.webflow.test.MockRequestContext;

public class GetPersonActionTests extends TestCase {

	public void testGetPerson() throws Exception {
		MockControl control = MockControl.createControl(PhoneBook.class);
		PhoneBook phoneBook = (PhoneBook)control.getMock();
		phoneBook.getPerson(new Long(1));
		control.setReturnValue(new Person(), 1);
		control.replay();

		GetPersonAction action = new GetPersonAction();
		action.setPhoneBook(phoneBook);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("id", new Long(1));
		Event result = action.execute(context);
		assertEquals("success", result.getId());
		Assert.attributePresent(context.getRequestScope(), "person");
		control.verify();
	}

	public void testGetPersonDoesNotExist() throws Exception {
		MockControl control = MockControl.createControl(PhoneBook.class);
		PhoneBook phoneBook = (PhoneBook)control.getMock();
		phoneBook.getPerson(new Long(2));
		control.setReturnValue(null, 1);
		control.replay();

		GetPersonAction action = new GetPersonAction();
		action.setPhoneBook(phoneBook);
		MockRequestContext context = new MockRequestContext();
		context.getFlowScope().setAttribute("id", new Long(2));
		Event result = action.execute(context);
		assertEquals("error", result.getId());
		Assert.attributeNotPresent(context.getRequestScope(), "person");
		control.verify();
	}
}