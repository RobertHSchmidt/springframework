package org.springframework.batch.item.file;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

/**
 * Tests for {@link ResourceLineReader}.
 */
public class ResourceLineReaderTests {

	// common value used for writing to a file
	private String TEST_STRING = "FlatFileInputTemplate-TestData";

	private ResourceLineReader<String> reader = new ResourceLineReader<String>();

	private ExecutionContext executionContext = new ExecutionContext();

	@Before
	public void setUp() {

		reader.setResource(getInputResource("testLine1\ntestLine2\ntestLine3\ntestLine4\ntestLine5\ntestLine6"));
		reader.setLineMapper(new PassThroughLineMapper());
	}

	@Test
	public void testRestartWithCustomRecordSeparatorPolicy() throws Exception {

		reader.setRecordSeparatorPolicy(new RecordSeparatorPolicy() {
			// 1 record = 2 lines
			boolean pair = true;

			public boolean isEndOfRecord(String line) {
				pair = !pair;
				return pair;
			}

			public String postProcess(String record) {
				return record;
			}

			public String preProcess(String record) {
				return record;
			}
		});

		reader.open(executionContext);

		assertEquals("testLine1testLine2", reader.read().toString());
		assertEquals("testLine3testLine4", reader.read().toString());

		reader.update(executionContext);

		reader.close(executionContext);

		reader.open(executionContext);

		assertEquals("testLine5testLine6", reader.read().toString());
	}

	@Test
	public void testRestartWithSkippedLines() throws Exception {

		reader.setLinesToSkip(2);
		reader.open(executionContext);

		// read some records
		reader.read();
		reader.read();
		// get restart data
		reader.update(executionContext);
		// read next two records
		reader.read();
		reader.read();

		assertEquals(2, executionContext.getLong(ClassUtils.getShortName(FlatFileItemReader.class) + ".read.count"));
		// close input
		reader.close(executionContext);

		reader
				.setResource(getInputResource("header\nignoreme\ntestLine1\ntestLine2\ntestLine3\ntestLine4\ntestLine5\ntestLine6"));

		// init for restart
		reader.open(executionContext);

		// read remaining records
		assertEquals("testLine3", reader.read().toString());
		assertEquals("testLine4", reader.read().toString());

		reader.update(executionContext);
		assertEquals(4, executionContext.getLong(ClassUtils.getShortName(FlatFileItemReader.class) + ".read.count"));
	}

	@Test
	public void testNonExistantResource() throws Exception {

		Resource resource = new NonExistentResource();

		reader.setResource(resource);

		// afterPropertiesSet should only throw an exception if the Resource is
		// null
		reader.afterPropertiesSet();

		reader.open(executionContext);
		assertNull(reader.read());

	}

	@Test
	public void testRuntimeFileCreation() throws Exception {

		Resource resource = new NonExistentResource();

		reader.setResource(resource);

		// afterPropertiesSet should only throw an exception if the Resource is
		// null
		reader.afterPropertiesSet();

		// replace the resource to simulate runtime resource creation
		reader.setResource(getInputResource(TEST_STRING));
		reader.open(executionContext);
		assertEquals(TEST_STRING, reader.read());
	}

	private Resource getInputResource(String input) {
		return new ByteArrayResource(input.getBytes());
	}

	private static class NonExistentResource extends AbstractResource {

		public NonExistentResource() {
		}

		public boolean exists() {
			return false;
		}

		public String getDescription() {
			return "NonExistantResource";
		}

		public InputStream getInputStream() throws IOException {
			return null;
		}
	}
}
