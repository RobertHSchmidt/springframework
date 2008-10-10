/**
 * 
 */
package org.springframework.batch.core;

import java.util.Date;
import java.util.Iterator;

import junit.framework.TestCase;

/**
 * @author Lucas Ward
 *
 */
public class JobParametersBuilderTests extends TestCase {

	JobParametersBuilder parametersBuilder = new JobParametersBuilder();
	
	Date date = new Date(System.currentTimeMillis());
	
	public void testToJobRuntimeParamters(){	
		parametersBuilder.addDate("SCHEDULE_DATE", date);
		parametersBuilder.addLong("LONG", new Long(1));
		parametersBuilder.addString("STRING", "string value");
		JobParameters parameters = parametersBuilder.toJobParameters();
		assertEquals(date, parameters.getDate("SCHEDULE_DATE"));
		assertEquals(1L, parameters.getLong("LONG"));
		assertEquals("string value", parameters.getString("STRING"));
	}

	public void testOrderedTypes(){	
		parametersBuilder.addDate("SCHEDULE_DATE", date);
		parametersBuilder.addLong("LONG", new Long(1));
		parametersBuilder.addString("STRING", "string value");
		Iterator<String> parameters = parametersBuilder.toJobParameters().getParameters().keySet().iterator();
		assertEquals("SCHEDULE_DATE", parameters.next());
		assertEquals("LONG", parameters.next());
		assertEquals("STRING", parameters.next());
	}

	public void testOrderedStrings(){	
		parametersBuilder.addString("foo", "value foo");
		parametersBuilder.addString("bar", "value bar");
		parametersBuilder.addString("spam", "value spam");
		Iterator<String> parameters = parametersBuilder.toJobParameters().getParameters().keySet().iterator();
		assertEquals("foo", parameters.next());
		assertEquals("bar", parameters.next());
		assertEquals("spam", parameters.next());
	}
}
