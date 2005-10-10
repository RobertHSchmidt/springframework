package org.springframework.webflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.springframework.core.io.AbstractResource;

public class JarFileResource extends AbstractResource {

	private JarFile jarFile;
	
	private ZipEntry entry;
	
	public JarFileResource(JarFile jarFile, ZipEntry entry) {
		this.jarFile = jarFile;
		this.entry = entry;
	}
	
	public JarFile getJarFile() {
		return jarFile;
	}
	
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
