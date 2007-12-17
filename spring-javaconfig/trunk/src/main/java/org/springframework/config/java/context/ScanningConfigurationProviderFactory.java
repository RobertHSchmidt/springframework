package org.springframework.config.java.context;

import org.springframework.config.java.annotation.Configuration;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Factory that encapsulates instantiation and configuration logic necessary to
 * create a {@link ClassPathScanningCandidateComponentProvider} able to select
 * JavaConfig &#64Configuration classes properly.
 * 
 * @author Chris Beams
 */
public class ScanningConfigurationProviderFactory {

	/**
	 * Return a new provider instance for use with <var>resource</var>.
	 * 
	 * <p/>TODO: implement caching?
	 * 
	 * @param resourceLoader
	 * @return new {@link ClassPathScanningCandidateComponentProvider}
	 */
	public ClassPathScanningCandidateComponentProvider getProvider(ResourceLoader resourceLoader) {
		// TODO: this shouldn't be enough... the include/exclude config below
		// should actually eliminate cases where the candidate class is nested
		// within a non-@Configuration class. Verify that this is being tested.
		ClassPathScanningCandidateComponentProvider scanner;
		scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		scanner.addExcludeFilter(new NestedClassTypeFilter());
		scanner.setResourceLoader(resourceLoader);
		return scanner;
	}

}
