package org.springframework.config.java.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * TODO: Review: subclassed CPSCCP, but it didn't have sufficient visibility on
 * certain fields. I changed three fields from private->protected...
 * 
 * @author Chris Beams
 */
public class ConfigurationScanner extends ClassPathScanningCandidateComponentProvider {
	public ConfigurationScanner(ResourceLoader loader) {
		super(false);
		this.addIncludeFilter(new AnnotationTypeFilter(Configuration.class));
		this.setResourceLoader(loader);
	}

	public List<Class<?>> scanPackage(String configLocation) {
		return this.findCandidateClasses(configLocation);
	}

	/**
	 * Returns a List of all classes annotated as &#64;{@link Configuration},
	 * in order of being encountered. If a class is encountered twice, the first
	 * position in the list is retained. Duplicates in the list are not allowed.
	 * 
	 * <p/>TODO: inner classes included in the list?
	 * 
	 * @param basePackage
	 * @return list of classes annotated with &#64;{@link Configuration}
	 */
	protected List<Class<?>> findCandidateClasses(String basePackage) {
		// this list must disallow duplicates. we'll have to manage that
		// programmatically
		List<Class<?>> candidates = new ArrayList<Class<?>>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ ClassUtils.convertClassNameToResourcePath(basePackage) + "/" + this.resourcePattern;
			Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
			for (int i = 0; i < resources.length; i++) {
				Resource resource = resources[i];
				MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
				if (isCandidateComponent(metadataReader)) {
					try {
						candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
					}
					catch (ClassNotFoundException e) {
						// TODO: handle exception properly
						throw new RuntimeException(e);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}
}
