/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.config.java.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for importing other {@link Configuration} classes. Serves as an
 * analog to the XML configuration's &lt;import/&gt; element.
 * 
 * <h3>Example 1: Importing a single configuration class</h3>
 * 
 * <pre>
 * &#064;Import(InfrastructureConfig.class)
 * &#064;Configuration
 * public class MiddleTierConfig {
 * 	&#064;Bean public FooService fooService() { ... }
 * }
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
 * //...
 * </pre>
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
