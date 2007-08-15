package org.springframework.batch.sample.mapping;

import java.math.BigDecimal;

import org.springframework.batch.io.file.FieldSet;
import org.springframework.batch.io.file.FieldSetMapper;
import org.springframework.batch.sample.domain.Trade;
import org.springframework.batch.sample.mapping.TradeFieldSetMapper;

public class TradeFieldSetMapperTests extends AbstractFieldSetMapperTests{

	private static final String CUSTOMER = "Mike Tomcat";
	private static final BigDecimal PRICE = new BigDecimal(1.3);
	private static final long QUANTITY = 7;
	private static final String ISIN = "fj893gnsalX";

	protected Object expectedDomainObject() {
		Trade trade = new Trade();
		trade.setIsin(ISIN);
		trade.setQuantity(QUANTITY);
		trade.setPrice(PRICE);
		trade.setCustomer(CUSTOMER);
		return trade;
	}

	protected FieldSet fieldSet() {
		String[] tokens = new String[4];
		tokens[TradeFieldSetMapper.ISIN_COLUMN] = ISIN;
		tokens[TradeFieldSetMapper.QUANTITY_COLUMN] = String.valueOf(QUANTITY);
		tokens[TradeFieldSetMapper.PRICE_COLUMN] = String.valueOf(PRICE);
		tokens[TradeFieldSetMapper.CUSTOMER_COLUMN] = CUSTOMER;
		
		return new FieldSet(tokens);
	}

	protected FieldSetMapper fieldSetMapper() {
		return new TradeFieldSetMapper();
	}

}
