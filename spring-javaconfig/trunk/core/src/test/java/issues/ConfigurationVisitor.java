package issues;

import java.util.LinkedList;
import java.util.Queue;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.commons.EmptyVisitor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.config.java.annotation.Bean;
import org.springframework.util.ClassUtils;

public class ConfigurationVisitor extends EmptyVisitor {
	final RootBeanDefinition rbd = new RootBeanDefinition();
	final Queue<String> methodQueue = new LinkedList<String>();
	//((BeanDefinitionRegistry) bf).registerBeanDefinition("abc123", rbd);
	private final BeanFactory bf;


	public ConfigurationVisitor(BeanFactory bf) {
		this.bf = bf;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		System.out.printf("{%s,%s,%s,%s,%s}\n", version, access, name, signature, superName, interfaces);
		System.out.println("name: " + name);
		rbd.setBeanClassName(ClassUtils.convertResourcePathToClassName(name));
	}

	@Override
	public void visitEnd() {
		methodQueue.poll();
		super.visitEnd();
	}


	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		methodQueue.add(name + " " + desc);
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String annoDesc, boolean isVisible) {
		System.out.printf("anno desc: %s\n", annoDesc);
		if(!methodQueue.isEmpty())
			if(Bean.class.getName().equals(Util.convertTypeDescriptorToClassName(annoDesc))) {
				System.out.println("found bean annotation on " + methodQueue.peek());
			}
		/*
					if(Bean.class.getName().equals(ClassUtils.convertResourcePathToClassName(annoDesc)))
						System.out.printf("anno desc: %s\n", annoDesc);
		 */
		return super.visitAnnotation(annoDesc, isVisible);
	}

	@Override
	public AnnotationVisitor visitArray(String arg0) {
		System.out.println("visitArray arg0: " + arg0);
		return super.visitArray(arg0);
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		System.out.println("visitAnnotationDefault");
		return super.visitAnnotationDefault();
	}

	@Override
	public void visitEnum(String arg0, String arg1, String arg2) {
		System.out.printf("visitEnum {arg0=%s,arg1=%s,arg2=%s}\n", arg0, arg1, arg2);
		super.visitEnum(arg0, arg1, arg2);
	}

	@Override
	public void visit(String arg0, Object arg1) {
		System.out.printf("visit {arg0=%s,arg1=%s}\n", arg0, arg1);
		super.visit(arg0, arg1);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		System.out.printf("visitParameterAnnotation {arg0=%d,arg1=%s,arg2=%s}\n", arg0, arg1, arg2);
		return super.visitParameterAnnotation(arg0, arg1, arg2);
	}

}
