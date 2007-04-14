/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.config.java.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.asm.ClassReader;

/**
 * @author Rod Johnson
 * @author Costin Leau
 */
public abstract class AbstractClassTestingTypeFilter implements TypeFilter {

	protected final Log log = LogFactory.getLog(getClass());


	public final boolean match(ClassReader cr) {
		ClassNameVisitor v = new ClassNameVisitor();
		cr.accept(v, false);
		return match(v);
	}

	/**
	 * @param v
	 * @return
	 */
	protected abstract boolean match(ClassNameVisitor v);

}
