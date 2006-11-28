package org.springframework.beans.factory.java.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

public class GetterInvocationFindingClassVisitorTests extends TestCase {
	
	public void testOneGetterOnOneBean() throws IOException {
		MethodVisitor mv = null;// new MethodVisitor();
		// TODO use spring resource loading
		ResourceLoader rl = new DefaultResourceLoader();
		String location = "classpath:org/springframework/beans/factory/java/ConfigurationProcessorTests$RequiresProperty.class";
		InputStream is = //ConfigurationPostProcessorTests.class.getResourceAsStream(classPath);
			rl.getResource(location).getInputStream();
		assertNotNull(location);
		ClassReader cr = new ClassReader(is);
		GetterInvocationFindingClassVisitor gifcv = new GetterInvocationFindingClassVisitor();
		cr.accept(gifcv, false);
		
		List<String> gettersInvokedInCostinMethod = gifcv.getGetterInvocations().get("costin");
		//assertEquals("Found getter invocation in costin method", 1, gettersInvokedInCostinMethod.size());
	}

}
