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
package org.springframework.batch.io.driving;

import org.springframework.batch.io.driving.support.IbatisKeyGenerator;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Extension of {@link DrivingQueryInputSource} that maps keys to
 * objects.  An iBatis query id must be set to map and return each 'detail record'.
 *
 * @author Lucas Ward
 * @see IbatisKeyGenerator
 */
public class IbatisDrivingQueryInputSource extends DrivingQueryInputSource {

	private String detailsQueryId;

	private SqlMapClientTemplate sqlMapClientTemplate;

	/**
	 * Overridden read() that uses the returned key as arguments to the details query.
	 *
	 * @see org.springframework.batch.io.driving.DrivingQueryInputSource#read()
	 */
	public Object read() {
		Object key = super.read();
		if (key==null) {
			return null;
		}
		return sqlMapClientTemplate.queryForObject(detailsQueryId, key);
	}

	/**
	 * @param detailsQueryId id of the iBATIS select statement that will used
	 * to retrieve an object for a single primary key from the list
	 * returned by driving query
	 */
	public void setDetailsQueryId(String detailsQueryId) {
		this.detailsQueryId = detailsQueryId;
	}
	
	/**
	 * Set the {@link SqlMapClientTemplate} to use for this input source.
	 * 
	 * @param sqlMapClientTemplate
	 */
	public void setSqlMapClient(
			SqlMapClient sqlMapClient) {
		this.sqlMapClientTemplate = new SqlMapClientTemplate(sqlMapClient);
	}
}
