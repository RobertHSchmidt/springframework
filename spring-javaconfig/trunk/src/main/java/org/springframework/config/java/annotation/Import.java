package org.springframework.config.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for importing other {@link Configuration} classes. Serves as an
 * analog to the XML configuration's &lt;import/&gt; tag.
 * 
 * <h3>Example 1: Importing a single configuration class</h3>
 * 
 * <pre>
 * &#064;Import(InfrastructureConfig.class)
 * &#064;Configuration
 * public class MiddleTierConfig {
 * 	&#064;Bean public FooService fooService() { ... }
 * }
 * 
 * &#064;Configuration
 * public class InfrastructureConfig {
 * 	&#064;Bean public DataSource dataSource() { ... }
 * }
 * </pre>
 * 
 * <h3>Example 2: Importing multiple configuration classes</h3>
 * 
 * <pre>
 * &#064;Import({ InfrastructureConfig.class, AspectConfig.class })
 * &#064;Configuration
 * public class MiddleTierConfig {
 * 	&#064;Bean public FooService fooService() { ... }
 * }
 * 
 * //...
 * </pre>
 * 
 * Original feature request:
 * http://opensource.atlassian.com/projects/spring/browse/SJC-34
 * 
 * @see Configuration
 * 
 * @param value - one or more classes that are a) annotated with
 * &#64;Configuration or b) implicitly considered Configuration classes by
 * virtue of declaring &#64;Bean methods
 * 
 * @author Chris Beams
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Import {
	Class<?>[] value();
}
