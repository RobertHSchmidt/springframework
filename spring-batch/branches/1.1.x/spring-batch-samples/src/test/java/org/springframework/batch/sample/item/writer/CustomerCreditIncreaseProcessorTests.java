package org.springframework.batch.sample.item.writer;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.springframework.batch.sample.dao.CustomerCreditDao;
import org.springframework.batch.sample.domain.CustomerCredit;

/**
 * Tests for {@link CustomerCreditIncreaseWriter}.
 * 
 * @author Robert Kasanicky
 */
public class CustomerCreditIncreaseProcessorTests extends TestCase {

	private CustomerCreditIncreaseWriter writer = new CustomerCreditIncreaseWriter();

	/**
	 * Increases customer's credit by fixed value
	 */
	public void testProcess() throws Exception {
		
		final BigDecimal oldCredit = new BigDecimal(10.54);
		class CustomerDaoStub implements CustomerCreditDao {

			public void writeCredit(CustomerCredit customerCredit) throws Exception {
				BigDecimal expectedCredit = oldCredit.add(CustomerCreditIncreaseWriter.FIXED_AMOUNT);
				assertTrue(customerCredit.getCredit().compareTo(expectedCredit) == 0);

			}

		}
		CustomerCredit customerCredit = new CustomerCredit();
		customerCredit.setId(1);
		customerCredit.setName("testCustomer");
		writer.setCustomerCreditDao(new CustomerDaoStub());

		customerCredit.setCredit(oldCredit);

		writer.write(customerCredit);

	}
}
