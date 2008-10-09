/**
 * 
 */
package org.springframework.batch.sample.domain.trade;

import java.math.BigDecimal;

import org.springframework.batch.item.file.mapping.FieldSet;
import org.springframework.batch.item.file.mapping.FieldSetMapper;

/**
 * {@link FieldSetMapper} for mapping the 
 * 
 * @author Lucas Ward
 *
 */
public class CustomerUpdateFieldSetMapper implements FieldSetMapper<CustomerUpdate> {

	public CustomerUpdate map(FieldSet fs) {
		
		CustomerOperation operation = CustomerOperation.fromCode(fs.readChar(0));
		String name = fs.readString(1);
		BigDecimal credit = fs.readBigDecimal(2);
		
		return  new CustomerUpdate(operation, name, credit);
	}
	
}
