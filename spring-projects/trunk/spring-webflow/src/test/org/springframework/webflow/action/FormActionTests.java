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
package org.springframework.webflow.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * Unit test for the FormAction class.
 * 
 * @see org.springframework.webflow.action.FormAction
 * 
 * @author Erwin Vervaet
 */
public class FormActionTests extends TestCase {
	
	private static class TestBean {
		
		private String prop;
		
		public TestBean() {
		}
		
		public TestBean(String prop) {
			this.prop = prop;
		}
		
		public String getProp() {
			return prop;
		}
		
		public void setProp(String prop) {
			this.prop = prop;
		}
	}
	
	private static class TestBeanValidator implements Validator {
		public boolean supports(Class clazz) {
			return TestBean.class.equals(clazz);
		}
		
		public void validate(Object formObject, Errors errors) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "prop", "Prop cannot be empty");
		}
	}
	
	private FormAction action;
	
	protected void setUp() throws Exception {
		action = new FormAction();
		action.setFormObjectName("test");
		action.setFormObjectClass(TestBean.class);
		action.setValidator(new TestBeanValidator());
		action.setFormObjectScope(ScopeType.FLOW);
		action.setErrorsScope(ScopeType.REQUEST);
		action.setValidateOnBinding(true);
		action.initAction();
	}
	
	public void testSetupForm() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this, "test", params("prop", "value")));
		
		// setupForm() should initialize the form object and the Errors
		// instance, but no bind & validate should happen since bindOnSetupForm
		// is not set
		
		assertEquals(AbstractAction.SUCCESS_EVENT_ID, action.setupForm(context).getId());
		
		assertEquals(2, context.getRequestScope().size());
		assertEquals(2, context.getFlowScope().size());
		assertFalse(getErrors(context).hasErrors());
		assertNull(getFormObject(context).getProp());
	}
	
	public void testSetupFormWithExistingFormObject() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this, "test", params("prop", "value")));
		
		assertEquals(AbstractAction.SUCCESS_EVENT_ID, action.setupForm(context).getId());
		
		Errors errors = getErrors(context);
		errors.reject("dummy");
		TestBean formObject = getFormObject(context);
		formObject.setProp("bla");
		
		// setupForm() should leave the existing form object and Errors instance
		// untouched, at least when no bind & validate is done (bindOnSetupForm == false)

		assertEquals(AbstractAction.SUCCESS_EVENT_ID, action.setupForm(context).getId());

		assertEquals(2, context.getRequestScope().size());
		assertEquals(2, context.getFlowScope().size());
		assertSame(errors, getErrors(context));
		assertSame(formObject, getFormObject(context));
		assertTrue(getErrors(context).hasErrors());
		assertEquals("bla", getFormObject(context).getProp());
	}
	
	public void testSetupFormFailure() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this));
		
		action.setBindOnSetupForm(true);
		
		// setupForm() should setup 
		
		assertEquals(AbstractAction.ERROR_EVENT_ID, action.setupForm(context).getId());
		
		assertEquals(2, context.getRequestScope().size());
		assertEquals(2, context.getFlowScope().size());
		assertTrue(getErrors(context).hasErrors());
		assertNull(getFormObject(context).getProp());
	}
	
	// this is what happens in a 'form state'
	public void testBindAndValidateFailureThenSetupForm() throws Exception {
		MockRequestContext context = new MockRequestContext();
		context.setLastEvent(new Event(this, "test", params("prop", "")));
		
		// setup existing form object & errors
		assertEquals(AbstractAction.SUCCESS_EVENT_ID, action.setupForm(context).getId());		
		TestBean formObject = getFormObject(context);
		formObject.setProp("bla");
		
		assertEquals(AbstractAction.ERROR_EVENT_ID, action.bindAndValidate(context).getId());

		assertEquals(2, context.getRequestScope().size());
		assertEquals(2, context.getFlowScope().size());
		assertSame(formObject, getFormObject(context));
		assertTrue(getErrors(context).hasErrors());
		assertEquals("", getFormObject(context).getProp());

		Errors errors = getErrors(context);

		assertEquals(AbstractAction.SUCCESS_EVENT_ID, action.setupForm(context).getId());
		
		assertEquals(2, context.getRequestScope().size());
		assertEquals(2, context.getFlowScope().size());
		assertSame(errors, getErrors(context));
		assertSame(formObject, getFormObject(context));
		assertTrue(getErrors(context).hasErrors());
		assertEquals("", getFormObject(context).getProp());
	}
	
	// helpers
	
	private Errors getErrors(RequestContext context) {
		return (Errors)context.getRequestScope().get(BindException.ERROR_KEY_PREFIX + "test");
	}
	
	private TestBean getFormObject(RequestContext context) {
		return (TestBean)context.getFlowScope().get("test");
	}

	private Map params(String key, String value) {
		Map res = new HashMap(1);
		res.put(key, value);
		return res;
	}
}
