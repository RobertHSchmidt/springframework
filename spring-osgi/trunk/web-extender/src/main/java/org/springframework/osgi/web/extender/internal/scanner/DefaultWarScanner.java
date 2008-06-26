/*
 * Copyright 2006-2008 the original author or authors.
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

package org.springframework.osgi.web.extender.internal.scanner;

import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

/**
 * Scanner for WAR applications. Looks for the presence of
 * <code>WEB-INF/web.xml</code>.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultWarScanner implements WarScanner {

	/** default folder */
	private static final String WEB_INF = "WEB-INF/";
	/** default configuration file */
	private static final String WEB_XML = "web.xml";


	public URL getWebXmlConfiguration(Bundle bundle) {
		if (bundle == null)
			return null;

		// look into the bundle space for web.xml
		Enumeration enm = bundle.findEntries(WEB_INF, WEB_XML, false);
		if (enm != null && enm.hasMoreElements())
			return (URL) enm.nextElement();
		return null;
	}
}
