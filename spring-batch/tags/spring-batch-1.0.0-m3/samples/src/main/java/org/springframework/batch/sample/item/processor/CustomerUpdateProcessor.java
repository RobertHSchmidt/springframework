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

package org.springframework.batch.sample.item.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.sample.dao.JdbcCustomerDebitWriter;
import org.springframework.batch.sample.domain.CustomerDebit;
import org.springframework.batch.sample.domain.Trade;


/**
 * Transforms Trade to a CustomerDebit and asks dao object to write the result.
 * 
 * @author Robert Kasanicky
 */
public class CustomerUpdateProcessor implements ItemProcessor {
    private JdbcCustomerDebitWriter dao;

    public void process(Object data) {
        Trade trade = (Trade) data;
        CustomerDebit customerDebit = new CustomerDebit();
        customerDebit.setName(trade.getCustomer());
        customerDebit.setDebit(trade.getPrice());
        dao.write(customerDebit);
    }

    public void setDao(JdbcCustomerDebitWriter outputSource) {
        this.dao = outputSource;
    }

	public void close() {
	}

	public void init() {
	}
}
