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
package org.springframework.config.java;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Costin Leau
 * @author Chris Beams
 */
@ContextConfiguration(locations = { "classpath:org/springframework/config/java/scoping.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class ScopingXmlTests implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Before
	public void setUp() {
		ScopingTests.flag = "1";
	}

	@Test
	public void testGetScopedBean() {
		TestBean bean = (TestBean) applicationContext.getBean("scopedProxyClass");
		assertNotNull(bean);
		assertTrue("scoped bean is not wrapped by the scoped-proxy", bean instanceof ScopedObject);
		assertEquals("1", bean.getName());

		ITestBean interfaceBean = (ITestBean) applicationContext.getBean("scopedProxyClass");
		assertNotNull(interfaceBean);
		assertEquals("1", interfaceBean.getName());
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
