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

package org.springframework.batch.sample.item.reader;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.mapping.FieldSet;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.support.DelegatingItemReader;
import org.springframework.batch.sample.domain.Address;
import org.springframework.batch.sample.domain.BillingInfo;
import org.springframework.batch.sample.domain.Customer;
import org.springframework.batch.sample.domain.LineItem;
import org.springframework.batch.sample.domain.Order;
import org.springframework.batch.sample.domain.ShippingInfo;

/**
 * @author peter.zozom
 * 
 */
public class OrderItemReader extends DelegatingItemReader<Order> {
	private static Log log = LogFactory.getLog(OrderItemReader.class);

	private Order order;

	private boolean recordFinished;

	private FieldSetMapper<Order> headerMapper;

	private FieldSetMapper<Customer> customerMapper;

	private FieldSetMapper<Address> addressMapper;

	private FieldSetMapper<BillingInfo> billingMapper;

	private FieldSetMapper<LineItem> itemMapper;

	private FieldSetMapper<ShippingInfo> shippingMapper;

	/**
	 * @throws Exception
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	public Order read() throws Exception {
		recordFinished = false;

		while (!recordFinished) {
			process((FieldSet) super.read());
		}

		log.info("Mapped: " + order);
		
		Order result = order;
		order = null;

		return result;
	}

	/**
	 * @see org.springframework.batch.execution.io.FieldSetCallback#execute(StepExecution)
	 */
	private void process(FieldSet fieldSet) {
		// finish processing if we hit the end of file
		if (fieldSet == null) {
			log.debug("FINISHED");
			recordFinished = true;
			order = null;

			return;
		}

		String lineId = fieldSet.readString(0);

		// start a new Order
		if (Order.LINE_ID_HEADER.equals(lineId)) {
			log.debug("STARTING NEW RECORD");
			order = headerMapper.mapLine(fieldSet, -1);

			return;
		}

		// mark we are finished with current Order
		if (Order.LINE_ID_FOOTER.equals(lineId)) {
			log.debug("END OF RECORD");

			// Do mapping for footer here, because mapper does not allow to pass
			// an Order object as input.
			// Mapper always creates new object
			order.setTotalPrice(fieldSet.readBigDecimal("TOTAL_PRICE"));
			order.setTotalLines(fieldSet.readInt("TOTAL_LINE_ITEMS"));
			order.setTotalItems(fieldSet.readInt("TOTAL_ITEMS"));

			recordFinished = true;

			return;
		}

		if (Customer.LINE_ID_BUSINESS_CUST.equals(lineId)) {
			log.debug("MAPPING CUSTOMER");

			if (order.getCustomer() == null) {
				order.setCustomer(customerMapper.mapLine(fieldSet, -1));
				order.getCustomer().setBusinessCustomer(true);
			}

			return;
		}

		if (Customer.LINE_ID_NON_BUSINESS_CUST.equals(lineId)) {
			log.debug("MAPPING CUSTOMER");

			if (order.getCustomer() == null) {
				order.setCustomer(customerMapper.mapLine(fieldSet, -1));
				order.getCustomer().setBusinessCustomer(false);
			}

			return;
		}

		if (Address.LINE_ID_BILLING_ADDR.equals(lineId)) {
			log.debug("MAPPING BILLING ADDRESS");
			order.setBillingAddress(addressMapper.mapLine(fieldSet, -1));
			return;
		}

		if (Address.LINE_ID_SHIPPING_ADDR.equals(lineId)) {
			log.debug("MAPPING SHIPPING ADDRESS");
			order.setShippingAddress(addressMapper.mapLine(fieldSet, -1));
			return;
		}

		if (BillingInfo.LINE_ID_BILLING_INFO.equals(lineId)) {
			log.debug("MAPPING BILLING INFO");
			order.setBilling(billingMapper.mapLine(fieldSet, -1));
			return;
		}

		if (ShippingInfo.LINE_ID_SHIPPING_INFO.equals(lineId)) {
			log.debug("MAPPING SHIPPING INFO");
			order.setShipping(shippingMapper.mapLine(fieldSet, -1));
			return;
		}

		if (LineItem.LINE_ID_ITEM.equals(lineId)) {
			log.debug("MAPPING LINE ITEM");

			if (order.getLineItems() == null) {
				order.setLineItems(new ArrayList<LineItem>());
			}
			order.getLineItems().add(itemMapper.mapLine(fieldSet, -1));

			return;
		}

		log.debug("Could not map LINE_ID=" + lineId);

	}

	public void setAddressMapper(FieldSetMapper<Address> addressMapper) {
		this.addressMapper = addressMapper;
	}

	public void setBillingMapper(FieldSetMapper<BillingInfo> billingMapper) {
		this.billingMapper = billingMapper;
	}

	public void setCustomerMapper(FieldSetMapper<Customer> customerMapper) {
		this.customerMapper = customerMapper;
	}

	public void setHeaderMapper(FieldSetMapper<Order> headerMapper) {
		this.headerMapper = headerMapper;
	}

	public void setItemMapper(FieldSetMapper<LineItem> itemMapper) {
		this.itemMapper = itemMapper;
	}

	public void setShippingMapper(FieldSetMapper<ShippingInfo> shippingMapper) {
		this.shippingMapper = shippingMapper;
	}

}
