/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.binding.expression.el;

import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.springframework.binding.collection.MapAdaptable;

/**
 * An {@link ELResolver} for properly resolving variables in an instance of {@link MapAdaptable}
 * @author Jeremy Grelle
 */
public class MapAdaptableELResolver extends ELResolver {

	public Class getCommonPropertyType(ELContext context, Object base) {
		if (base instanceof MapAdaptable) {
			return Object.class;
		}
		return null;
	}

	public Iterator getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	public Class getType(ELContext context, Object base, Object property) throws NullPointerException,
			PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException("The ELContext is null.");
		}

		if (base instanceof MapAdaptable) {
			context.setPropertyResolved(true);
			Object obj = adapt(base).get(property);
			return (obj != null) ? obj.getClass() : null;
		}

		return null;
	}

	public Object getValue(ELContext context, Object base, Object property) throws NullPointerException,
			PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException("The ELContext is null.");
		}

		if (base instanceof MapAdaptable) {
			context.setPropertyResolved(true);
			return adapt(base).get(property);
		}

		return null;
	}

	public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException,
			PropertyNotFoundException, ELException {
		if (context == null) {
			throw new NullPointerException("The ELContext is null.");
		}

		if (base instanceof MapAdaptable) {
			context.setPropertyResolved(true);
		}

		return false;
	}

	public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException,
			PropertyNotFoundException, PropertyNotWritableException, ELException {
		if (context == null) {
			throw new NullPointerException("The ELContext is null.");
		}

		if (base instanceof MapAdaptable) {
			context.setPropertyResolved(true);

			try {
				adapt(base).put(property, value);
			} catch (UnsupportedOperationException e) {
				throw new PropertyNotWritableException(e);
			}
		}
	}

	private Map adapt(Object base) {
		MapAdaptable adaptable = (MapAdaptable) base;
		return adaptable.asMap();
	}

}
