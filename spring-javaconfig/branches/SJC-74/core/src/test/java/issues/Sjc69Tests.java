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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;
import org.springframework.config.java.context.JavaConfigApplicationContext;

/**
 * SJC-69 revaled a bug in the way addConfigClass() works with regard to bean
 * overrides. As Mark points out, the current (buggy) functionality is that bean
 * overriding is happening in a first-in-first-out fashion. It should be
 * last-in-first-out.
 * 
 * @author Mark Rohan
 * @author Chris Beams
 */
public class Sjc69Tests {

	public static interface MyBean {
		String getName();
	}

	public static class MyBeanA implements MyBean {
		public String getName() {
			return "BeanA";
		}
	}

	public static class MyBeanB implements MyBean {
		public String getName() {
			return "BeanB";
		}
	}

	@Configuration
	public static class MyConfigA {
		@Bean
		public MyBean myBean() {
			return new MyBeanA();
		}
	}

	@Configuration
	public static class MyConfigB {
		@Bean
		public MyBean myBean() {
			return new MyBeanB();
		}
	}

	public static class MySpringContext extends JavaConfigApplicationContext {
		public MySpringContext() {
			super();
			addConfigClass(MyConfigA.class);
			addConfigClass(MyConfigB.class);
			refresh();
		}
	}

	@Ignore
	@Test
	public void reproPerOriginalBugReport() {
		MySpringContext msc = new MySpringContext();
		MyBean mb = msc.getBean(MyBean.class);
		assertNotNull(mb);
		assertThat(mb, instanceOf(MyBeanB.class));
	}

	/**
	 * Simply demonstrates the bug applies when using the
	 * JavaConfigApplicationContext API directly as well.
	 */
	@Ignore
	@Test
	public void reproWithJavaConfigApplicationContext() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.addConfigClass(MyConfigA.class);
		ctx.addConfigClass(MyConfigB.class);
		ctx.refresh();
		MyBean mb = ctx.getBean(MyBean.class);
		assertNotNull(mb);
		assertThat(mb, instanceOf(MyBeanB.class));
	}

	/**
	 * To work around this issue until fixed, simply reverse the ordering.
	 */
	@Test
	public void workaround() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext();
		ctx.addConfigClass(MyConfigB.class);
		ctx.addConfigClass(MyConfigA.class); // reversed
		ctx.refresh();
		MyBean mb = ctx.getBean(MyBean.class);
		assertNotNull(mb);
		assertThat(mb, instanceOf(MyBeanB.class));
	}
}