package org.springframework.webflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.springframework.core.io.AbstractResource;

/**
 * A resource providing access to the input stream of a jar file entry.
 * @author Keith Donald
 */
public class JarFileResource extends AbstractResource {

	/**
	 * The Jar File where the resource resides
	 */
	private JarFile jarFile;

	/**
	 * The entry in the jar file representing this resource.
	 */
	private ZipEntry entry;

	/**
	 * Constructs a new jar file resource.
	 * 
	 * @param jarFile the jar file
	 * @param entry the jar file entry
	 */
	public JarFileResource(JarFile jarFile, ZipEntry entry) {
		this.jarFile = jarFile;
		this.entry = entry;
	}

	/**
	 * Returns the JAR file.
	 */
	public JarFile getJarFile() {
		return jarFile;
	}

	/**
	 * Returns the JAR file zip entry representing this resource.
	 */
	public ZipEntry getEntry() {
		return entry;
	}

	public String getDescription() {
		return entry.getName();
	}

	public InputStream getInputStream() throws IOException {
		return jarFile.getInputStream(entry);
	}
}