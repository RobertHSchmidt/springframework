package org.springframework.batch.core.configuration.support;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * 
 * @author Lucas Ward
 *
 */
public class ClassPathXmlJobRegistryTests {

	ClassPathXmlJobRegistry registry;
	
	@Test
	public void testLocateJob() throws Exception{
		
		List<Resource> jobPaths = new ArrayList<Resource>();
		jobPaths.add(new ClassPathResource("org/springframework/batch/core/launch/support/job.xml"));
		jobPaths.add(new ClassPathResource("org/springframework/batch/core/launch/support/job2.xml"));
		
		registry = new ClassPathXmlJobRegistry(jobPaths);
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.refresh();
		registry.setApplicationContext(applicationContext);
		registry.afterPropertiesSet();
		
		Collection<String> names = registry.getJobNames();
		assertEquals(2, names.size());
		assertTrue(names.contains("test-job"));
		assertTrue(names.contains("test-job2"));
		
		Job job = registry.getJob("test-job");
		assertEquals("test-job", job.getName());
		job = registry.getJob("test-job2");
		assertEquals("test-job2", job.getName());
	}
	
	@Test(expected=NoSuchJobException.class)
	public void testNoJobFound() throws Exception{
		
		List<Resource> jobPaths = new ArrayList<Resource>();
		jobPaths.add(new ClassPathResource("org/springframework/batch/core/launch/support/test-environment.xml"));
		registry = new ClassPathXmlJobRegistry(jobPaths);
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.refresh();
		registry.setApplicationContext(applicationContext);
		registry.afterPropertiesSet();
	}
	
	@Test(expected=DuplicateJobException.class)
	public void testDuplicateJobsInFile() throws Exception{
		
		List<Resource> jobPaths = new ArrayList<Resource>();
		jobPaths.add(new ClassPathResource("org/springframework/batch/core/launch/support/2jobs.xml"));
		registry = new ClassPathXmlJobRegistry(jobPaths);
		GenericApplicationContext applicationContext = new GenericApplicationContext();
		applicationContext.refresh();
		registry.setApplicationContext(applicationContext);
		registry.afterPropertiesSet();
	}
}
