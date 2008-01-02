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
class ClassPathScanningConfigurationProviderFactory {

	/**
	 * Return a new provider instance configured to treat as candidates only
	 * those classes that are both annotated with &#64Configuration and NOT a
	 * nested configuration class. Expressly eliminating the matching of nested
	 * configurations is important because of the semantics of treating
	 * declaring configuration classes as parent application contexts. If all
	 * configuration classes were to be treated as equals during classpath
	 * scanning, the nested classes would get picked up and risk inadvertently
	 * overriding their declaring classes' bean definitions. Eliminating them
	 * from scanning selection process requires callers to explicitly reference
	 * nested classes. Doing this allows guarantees the preservation of the
	 * context hierarchy/nested class semantics.
	 * 
	 * <p/>TODO: implement caching?
	 * 
	 * <p/>TODO: we currently advertise that &#Configuration is optional, and
	 * that the only hard requirement for a Configuration class is that it
	 * expose one or more non-private non-final methods annotated with &#64Bean.
	 * This implementation currently violates that, because it will only select
	 * those classes that are annotated at the class level with &#Configuration.
	 * Note that the contract is still respected when instantiating
	 * {@link org.springframework.config.java.context.JavaConfigApplicationContext}
	 * directly. Should probably come back and address this inconsistency just
	 * by making a note in the docs about it.
	 * 
	 * @param resourceLoader
	 * @return new {@link ClassPathScanningCandidateComponentProvider}
	 */
	public ClassPathScanningCandidateComponentProvider getProvider(ResourceLoader resourceLoader) {
		ClassPathScanningCandidateComponentProvider scanner;
		scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		scanner.addExcludeFilter(new NestedClassTypeFilter());
		scanner.setResourceLoader(resourceLoader);
		return scanner;
	}

}
