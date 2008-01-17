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

import org.springframework.batch.io.file.mapping.FieldSetMapper;
import org.springframework.batch.io.file.mapping.FieldSet;
import org.springframework.batch.sample.domain.Order;



public class HeaderFieldSetMapper implements FieldSetMapper {
	
	public static final String ORDER_ID_COLUMN = "ORDER_ID";
	public static final String ORDER_DATE_COLUMN = "ORDER_DATE";
	
    public Object mapLine(FieldSet fieldSet) {
        Order order = new Order();
        order.setOrderId(fieldSet.readLong(ORDER_ID_COLUMN));
        order.setOrderDate(fieldSet.readDate(ORDER_DATE_COLUMN));

        return order;
    }
}
