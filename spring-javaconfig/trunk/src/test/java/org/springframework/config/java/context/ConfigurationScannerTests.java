package org.springframework.config.java.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.config.java.context.forscanning.a.ConfigA1;
import org.springframework.config.java.context.forscanning.b.ConfigB1;
import org.springframework.config.java.context.forscanning.b.ConfigB2;
import org.springframework.core.io.DefaultResourceLoader;

public class ConfigurationScannerTests {
	private ConfigurationScanner configurationScanner;

	@Before
	public void setUp() {
		configurationScanner = new ConfigurationScanner(new DefaultResourceLoader());
	}

	@Test
	public void testScanPackageWithSingleClass() {
		List<Class<?>> configClasses = configurationScanner
				.scanPackage("org.springframework.config.java.context.forscanning.a");

		assertTrue(configClasses.contains(ConfigA1.class));
		assertEquals(1, configClasses.size());
	}

	@Test
	public void testScanPackageWithMultipleClass() {
		List<Class<?>> configClasses = configurationScanner
				.scanPackage("org.springframework.config.java.context.forscanning.b");

		assertTrue(configClasses.contains(ConfigB1.class));
		assertTrue(configClasses.contains(ConfigB2.class));
		assertEquals(2, configClasses.size());
	}

	@Test
	public void testScanPackageWithDepth() {
		packageContainsAllConfigClasses("org.springframework.config.java.context.forscanning");
	}

	@Test
	public void testScanPackageWithDepthAndWildcard1() {
		packageContainsAllConfigClasses("org.springframework.config.java.*.forscanning");
	}

	@Test
	public void testScanPackageWithDepthAndWildcard2() {
		packageContainsAllConfigClasses("org.springframework.config.**.for*");
	}

	private void packageContainsAllConfigClasses(String pkg) {
		List<Class<?>> configClasses = configurationScanner.scanPackage(pkg);

		assertTrue(configClasses.contains(ConfigA1.class));
		assertTrue(configClasses.contains(ConfigB1.class));
		assertTrue(configClasses.contains(ConfigB2.class));
		assertEquals(3, configClasses.size());
	}

}
