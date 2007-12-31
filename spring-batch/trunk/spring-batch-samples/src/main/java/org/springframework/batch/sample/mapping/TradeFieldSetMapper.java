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

package org.springframework.batch.sample.mapping;

import org.springframework.batch.io.file.mapping.FieldSet;
import org.springframework.batch.io.file.mapping.FieldSetMapper;
import org.springframework.batch.item.provider.AggregateItemReader;
import org.springframework.batch.sample.domain.Trade;



public class TradeFieldSetMapper implements FieldSetMapper {
	
	public static final int ISIN_COLUMN = 0;
	public static final int QUANTITY_COLUMN = 1;
	public static final int PRICE_COLUMN = 2;
	public static final int CUSTOMER_COLUMN = 3;
	
    public Object mapLine(FieldSet fieldSet) {
    	
    	if ("BEGIN".equals(fieldSet.readString(0))) {
    		return AggregateItemReader.BEGIN_RECORD;
    	}
    	
    	if ("END".equals(fieldSet.readString(0))) {
    		return AggregateItemReader.END_RECORD;
    	}

    	Trade trade = new Trade();
    	trade.setIsin(fieldSet.readString(0));
        trade.setQuantity(fieldSet.readLong(1));
        trade.setPrice(fieldSet.readBigDecimal(2));
        trade.setCustomer(fieldSet.readString(3));
        
        return trade;
    }
}
