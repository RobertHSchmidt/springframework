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

package org.springframework.osgi.blueprint;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceReference;

/**
 * @author Costin Leau
 */
public class ImporterListener {

	public static final List bind = new ArrayList();
	public static final List unbind = new ArrayList();


	public void bind(ServiceReference ref) {
		bind.add(ref);
	}

	public void unbind(ServiceReference ref) {
		unbind.add(ref);
	}

	public void refBind(ServiceReference ref) {
		bind.add(ref);
	}

	public void refUnbind(ServiceReference ref) {
		unbind.add(ref);
	}

	public void bindM(ServiceReference ref) {
		bind.add(ref);
	}

	public void unbindM(ServiceReference ref) {
		unbind.add(ref);
	}

	public void up(ServiceReference ref) {
		bind.add(ref);
	}

	public void down(ServiceReference ref) {
		unbind.add(ref);
	}
}