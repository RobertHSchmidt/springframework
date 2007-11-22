/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.sample.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.batch.repeat.ExitStatus;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatInterceptor;
import org.springframework.batch.sample.domain.CustomerCredit;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Lucas Ward
 * 
 */
public class HibernateCreditWriter extends HibernateDaoSupport implements
		CustomerCreditWriter, RepeatInterceptor {

	private int failOnFlush = -1;
	private List errors = new ArrayList();
	private Set failed = new HashSet();

	// TODO: these need to be ThreadLocal (or a pure framework concern).
	private RepeatContext context;
	private Set processed = new HashSet();

	/**
	 * Public accessor for the errors property.
	 * 
	 * @return the errors - a list of Throwable instances
	 */
	public List getErrors() {
		return errors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.sample.dao.CustomerCreditWriter#write(org.springframework.batch.sample.domain.CustomerCredit)
	 */
	public void writeCredit(CustomerCredit customerCredit) {
		if (customerCredit.getId() == failOnFlush) {
			// try to insert one with a duplicate ID
			CustomerCredit newCredit = new CustomerCredit();
			newCredit.setId(customerCredit.getId());
			newCredit.setName(customerCredit.getName());
			newCredit.setCredit(customerCredit.getCredit());
			getHibernateTemplate().save(newCredit);
		} else {
			getHibernateTemplate().update(customerCredit);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.io.OutputSource#write(java.lang.Object)
	 */
	public void write(Object output) {
		processed.add(output);
		writeCredit((CustomerCredit) output);
		if (failed.contains(output)) {
			// Force early completion to commit aggressively if we encounter a
			// failed item (from a failed chunk but we don't know which one was
			// the problem).
			context.setCompleteOnly();
			// Flush now, so that if there is a failure this record will be
			// skipped.
			getHibernateTemplate().flush();
		}
	}

	/**
	 * Public setter for the failOnFlush property.
	 * 
	 * @param failOnFlush
	 *            true if you want to fail on flush (for testing)
	 */
	public void setFailOnFlush(int failOnFlush) {
		this.failOnFlush = failOnFlush;
	}

	public void before(RepeatContext context) {
	}

	public void after(RepeatContext context, ExitStatus result) {
	}

	/**
	 * Flush the Hibernate session so that any batch exceptions are within the
	 * RepeatContext.
	 * 
	 * @see org.springframework.batch.repeat.RepeatInterceptor#close(org.springframework.batch.repeat.RepeatContext)
	 */
	public void close(RepeatContext context) {
		try {
			getHibernateTemplate().flush();
		} catch (RuntimeException e) {
			failed.addAll(processed);
			// onError will not be called after close() by the framework so we
			// have to do it here.
			onError(context, e);
			throw e;
		}
	}

	public void onError(RepeatContext context, Throwable e) {
		errors.add(e);
	}

	public void open(RepeatContext context) {
		this.context = context;
		errors.clear();
		processed.clear();
	}

}
