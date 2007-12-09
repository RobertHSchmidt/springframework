package org.springframework.config.java.context.forscanning.a;

import org.springframework.beans.TestBean;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.annotation.Configuration;

@Configuration
public class ConfigA1 {
	@Bean
	public TestBean testBean() {
		return new TestBean();
	}
}
