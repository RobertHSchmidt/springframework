package org.springframework.config.java.model;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.MetadataAwareAspectInstanceFactory;
import org.springframework.aop.aspectj.annotation.ReflectiveAspectJAdvisorFactory;
import org.springframework.aop.aspectj.annotation.SimpleMetadataAwareAspectInstanceFactory;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;


/**
 * Parses configuration class literals using java reflection, writing the results
 * out to a {@link ConfigurationModel} object.
 * <p/>
 * TODO: rename to ReflectiveConfigurationParser (see precedent
 * at ReflectiveAspectJAdvisorFactory)
 *
 * @author Chris Beams
 */
public class ReflectingConfigurationParser implements ConfigurationParser {

	private static final Log log = LogFactory.getLog(ReflectingConfigurationParser.class);

	private final ConfigurationModel model;

	public ReflectingConfigurationParser(ConfigurationModel model) {
		this.model = model;
	}

	/**
	 * Parse <var>configurationSource</var>, populating <tt>model</tt>.
	 *
	 * @param configurationSource class literal to be parsed via reflection
	 */
	public void parse(Object configurationSource) {
		Class<?> classLiteral = (Class<?>) configurationSource;
		model.add(doParse(classLiteral, false));
	}


	/**
	 *
	 * @param literalClass
	 * @param isDeclaringClass
	 * @return
	 */
	private ConfigurationClass doParse(final Class<?> literalClass, boolean isDeclaringClass) {
		final ConfigurationClass modelClass = createConfigurationClass(literalClass, isDeclaringClass);;

		// detect and process this configuration class's declaring class (if any)
		Class<?> declaringLiteralClass = literalClass.getDeclaringClass();
		if(new DeclaringClassInclusionPolicy().isCandidateForInclusion(declaringLiteralClass))
			modelClass.setDeclaringClass(doParse(declaringLiteralClass, true));

		// does this config class import any other config classes?
		Import importAnno = findAnnotation(literalClass, Import.class);
		if(importAnno != null)
			for(Class<?> classToImport : importAnno.value())
				modelClass.addImportedClass(doParse(classToImport, false));

		// is this an @Aspect configuration?
		final AspectJAdvisorFactory advisorFactory = new ReflectiveAspectJAdvisorFactory();
		// note: when porting to an ASM-based implementation, using this reflective aj
		// factory is going to pose a problem.  There is no ASM-based impl, and even the
		// interface expects class literals.  Should get in touch w/ Adrian et al on this
		final boolean isAtAspectClass = advisorFactory.isAspect(literalClass);
		if(isAtAspectClass)
			advisorFactory.validate(literalClass); // TODO: catch exceptions

		// iterate through all the methods in the specified configuration class
		// looking for @Bean, @ExternalBean, etc.
		ReflectionUtils.doWithMethods(
			// for each method in this class
			literalClass,
			// execute this callback
			new MethodCallback() {
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
        			Bean bean = findAnnotation(method, Bean.class);
        			if(bean != null)
        				modelClass.add(new BeanMethod(method.getName(), bean, method.getModifiers()));

        			ExternalBean extBean = findAnnotation(method, ExternalBean.class);
        			if(extBean != null)
        				modelClass.add(new ExternalBeanMethod(method.getName(), extBean, method.getModifiers()));

        			if(isAtAspectClass) {
        				// examine this method to see if it's an advice method
        				String aspectName = "aspectNamePlaceholder";
        				MetadataAwareAspectInstanceFactory aif = new SimpleMetadataAwareAspectInstanceFactory(literalClass, aspectName);
        				Advisor pa = advisorFactory.getAdvisor(method, aif, 0, aspectName);
                		if (pa != null && (pa instanceof PointcutAdvisor)) {
                			String adviceName = method.getName();
                			Advice advice = pa.getAdvice();
                			// advice may return null in the case of named pointcuts (@Pointcut)
                			if (advice != null) {
                				Pointcut pointcut = ((PointcutAdvisor) pa).getPointcut();
                				PointcutsAndAspectsHolder.pointcuts.put(adviceName, pointcut);
                				PointcutsAndAspectsHolder.advice.put(adviceName, advice);
                			}
                		}
        			}
    			}
    		},
    		// but exclude all Object.* methods
    		new MethodFilter() {
    			public boolean matches(Method method) {
    				if(method.getDeclaringClass().equals(Object.class))
    					return false;
    				return true;
    			}
    		}
    	);

		return modelClass;
	}

	private ConfigurationClass createConfigurationClass(Class<?> literal, boolean isDeclaringClass) {
		final ConfigurationClass modelClass;

		String className = literal.getName();
		Configuration metadata = AnnotationUtils.findAnnotation(literal, Configuration.class);
		int modifiers = literal.getModifiers();

		// treat declaring classes specially: they are only potentially configuration
		// classes -> the validation rules are different (see PCC class for details)
		if(isDeclaringClass)
			if(metadata == null)
				modelClass = new PotentialConfigurationClass(className, modifiers);
			else
				modelClass = new PotentialConfigurationClass(className, metadata, modifiers);
		// otherwise -> create a class as usual
		else
			if(metadata == null)
				modelClass = new ConfigurationClass(className, modifiers);
			else
				modelClass = new ConfigurationClass(className, metadata, modifiers);

		return modelClass;
	}



}

class DeclaringClassInclusionPolicy {
	public boolean isCandidateForInclusion(Class<?> declaringClass) {
		if(declaringClass == null) return false;
		if(declaringClass.getName().endsWith("Test")) return false;
		if(declaringClass.getName().endsWith("Tests")) return false;
		return true;
	}
}
