/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.config.java;

import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Costin Leau
 * 
 */
public class ScopingXmlTest extends AbstractDependencyInjectionSpringContextTests {

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:org/springframework/config/java/scoping.xml", };
	}

	@Override
	protected void onSetUp() throws Exception {
		ScopingTest.flag = "1";
	}

	public void testGetScopedBean() {
		TestBean bean = (TestBean) applicationContext.getBean("scopedProxyClass");
		assertNotNull(bean);
		assertTrue("scoped bean is not wrapped by the scoped-proxy", bean instanceof ScopedObject);
		assertEquals("1", bean.getName());

		ITestBean interfaceBean = (ITestBean) applicationContext.getBean("scopedProxyClass");
		assertNotNull(interfaceBean);
		assertEquals("1", interfaceBean.getName());
	}
}
