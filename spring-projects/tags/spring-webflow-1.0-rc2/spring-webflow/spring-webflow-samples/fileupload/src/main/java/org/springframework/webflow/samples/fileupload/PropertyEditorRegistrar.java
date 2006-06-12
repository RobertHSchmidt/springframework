package org.springframework.webflow.samples.fileupload;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

public class PropertyEditorRegistrar implements org.springframework.beans.PropertyEditorRegistrar {
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		// to actually be able to convert a multipart object to a byte[]
		// we have to register a custom editor (in this case the
		// ByteArrayMultipartFileEditor)
		registry.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
		// now Spring knows how to handle multipart objects and convert them
	}
}
