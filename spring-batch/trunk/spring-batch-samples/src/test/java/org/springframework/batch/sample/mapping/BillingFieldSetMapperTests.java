package org.springframework.batch.sample.mapping;

import org.springframework.batch.item.file.mapping.DefaultFieldSet;
import org.springframework.batch.item.file.mapping.FieldSet;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.sample.domain.BillingInfo;

public class BillingFieldSetMapperTests extends AbstractFieldSetMapperTests{

	private static final String PAYMENT_ID = "777";
	private static final String PAYMENT_DESC = "My last penny";
	
	protected Object expectedDomainObject() {
		BillingInfo bInfo = new BillingInfo();
		bInfo.setPaymentDesc(PAYMENT_DESC);
		bInfo.setPaymentId(PAYMENT_ID);
		return bInfo;
	}

	protected FieldSet fieldSet() {
		String[] tokens = new String[]{
				PAYMENT_ID, 
				PAYMENT_DESC};
		String[] columnNames = new String[]{
				BillingFieldSetMapper.PAYMENT_TYPE_ID_COLUMN,
				BillingFieldSetMapper.PAYMENT_DESC_COLUMN};
		return new DefaultFieldSet(tokens, columnNames);
	}

	protected FieldSetMapper<BillingInfo> fieldSetMapper() {
		return new BillingFieldSetMapper();
	}

}
