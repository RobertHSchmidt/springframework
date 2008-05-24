/*
 * Copyright 2002-2008 the original author or authors.
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
package issues;

import org.junit.Before;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.ExternalBean;
import org.springframework.config.java.context.ConfigurableJavaConfigApplicationContext;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.config.java.internal.model.MalformedJavaConfigurationException;

/**
 * SJC-54 revealed a bug wherein abstract Configurations would not be processed
 * unless they have at least one public Bean method. The appropriate behavior is
 * that abstract Configurations should be processed if the have any non-private
 * Bean methods. The example below is taken from Jim Moore's code originally
 * submitted with the bug.
 *
 * @author Chris Beams
 */
public class Sjc56Tests {
	private ConfigurableJavaConfigApplicationContext ctx;

	@Before
	public void initContext() {
		ctx = new JavaConfigApplicationContext();
		ctx.addConfigClass(InfrastructureConfig.class);
	}
    static class InfrastructureConfig {
    	public @Bean DataSource dataSource() { return new DataSource(); }
    }


	@Test
	public void withPublicExternalBean() {
		ctx.addConfigClass(RepositoryConfigWithPublicExternalBean.class);
		ctx.refresh();

		ctx.getBean("contactRepository");
	}
    static abstract class RepositoryConfigWithPublicExternalBean {
    	public @Bean JdbcContactRepository contactRepository() {
    		return new JdbcContactRepository(dataSource());
    	}
    	public abstract @ExternalBean DataSource dataSource();
    }


	@Test
	public void withPackagePrivateExternalBean() {
		ctx.addConfigClass(RepositoryConfigWithPackagePrivateExternalBean.class);
		ctx.refresh();

		ctx.getBean("contactRepository");
	}
    static abstract class RepositoryConfigWithPackagePrivateExternalBean {
    	public @Bean JdbcContactRepository contactRepository() {
    		return new JdbcContactRepository(dataSource());
    	}

    	abstract @ExternalBean DataSource dataSource();
    }


	@Test
	public void withProtectedExternalBean() {
		ctx.addConfigClass(RepositoryConfigWithProtectedExternalBean.class);
		ctx.refresh();

		// before this issue was fixed, this call would throw
		// NoSuchBeanDefinitionException, because the 'customerRepository' bean
		// was non-public (protected), and as such, the enclosing Configuration
		// class was ignored completely.
		ctx.getBean("contactRepository");
	}
    static abstract class RepositoryConfigWithProtectedExternalBean {
    	public @Bean JdbcContactRepository contactRepository() {
    		return new JdbcContactRepository(dataSource());
    	}

    	protected abstract @ExternalBean DataSource dataSource();
    }


	@Test(expected = MalformedJavaConfigurationException.class)
	public void withPrivateExternalBean() {
		ctx.addConfigClass(RepositoryConfigWithPrivateExternalBean.class);
		ctx.refresh();
	}
    static abstract class RepositoryConfigWithPrivateExternalBean {
    	public @Bean JdbcContactRepository contactRepository() {
    		return new JdbcContactRepository(dataSource());
    	}

    	private @ExternalBean DataSource dataSource() { return null; }
    }
}


class DataSource { }
class JdbcContactRepository {
	@SuppressWarnings("unused")
	public JdbcContactRepository(DataSource dataSource) { }
}