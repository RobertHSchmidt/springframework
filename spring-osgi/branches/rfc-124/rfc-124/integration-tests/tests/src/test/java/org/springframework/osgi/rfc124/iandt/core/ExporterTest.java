/*
 * Copyright 2006-2009 the original author or authors.
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

package org.springframework.osgi.rfc124.iandt.core;

import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.rfc124.iandt.BaseRFC124IntegrationTest;
import org.springframework.osgi.util.OsgiStringUtils;

/**
 * @author Costin Leau
 */
public class ExporterTest extends BaseRFC124IntegrationTest {

	@Override
	protected String[] getConfigLocations() {
		return new String[] { "org/springframework/osgi/rfc124/iandt/core/exporter-test.xml" };
	}

	public void testExportedServiceProperties() throws Exception {
		ServiceRegistration reg = (ServiceRegistration) applicationContext.getBean("simple");
		System.out.println(OsgiStringUtils.nullSafeToString(reg.getReference()));
	}
}
