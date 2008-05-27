package org.springframework.config.java.factory;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.config.java.annotation.Bean;
import org.springframework.config.java.context.JavaConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class BeanPostProcessorPropagationTests {
	public @Before void setUp() {
		TrackingBPP.beansSeen.clear();
	}

	public @Test void hiddenBeanPostProcessorIsAppliedToHiddenBeans() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(HiddenBPPConfig.class);

		//List<String> beansSeen = ctx.getBean(List.class);
		List<String> beansSeen = TrackingBPP.beansSeen;
		Assert.assertFalse("no beans were post processed", beansSeen.isEmpty());
		Assert.assertTrue("bpp did not process hidden bean", beansSeen.contains("hidden"));
	}
	public static class HiddenBPPConfig {
		@Bean TestBean hidden() {
			return new TestBean("originalHiddenName");
		}

		@Bean TrackingBPP trackingBpp() {
			return new TrackingBPP();
		}
	}


	/**
	 * Hidden BeanPostProcessors work perfectly well against hidden beans
	 * (see {@link BeanPostProcessorPropagationTests#hiddenBeanPostProcessorIsAppliedToHiddenBeans()})
	 * however, in the case of a visible bean accessing a hidden bean (which is likely to be the case
	 * in a real-world config scenario), the visible bean factory accesses a bean from the hidden
	 * bean factory before the hidden bean factory has been {@link AbstractApplicationContext#refresh() refreshed}.
	 * This means that {@link AbstractApplicationContext#registerBeanPostProcessors()} will not yet have been
	 * called, either.  The hidden bean will be instantiated and registered as a singleton without being
	 * post-processed properly.  The 'fix' for this issue is probably not worth the benefit of the feature.
	 * If users request support for hidden BeanPostProcessors, we'll reconsider it then.
	 */
	@Ignore // currently unsupported. see SJC-47 comments for details
	public @Test void hiddenBeanPostProcessorIsAppliedToHiddenBeansEvenWhenVisibleBeanAccessesHiddenBean() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(HiddenBPPWithReferenceFromVisibleBean.class);

		List<String> beansSeen = TrackingBPP.beansSeen;
		Assert.assertFalse("no beans were post processed", beansSeen.isEmpty());
		Assert.assertFalse("visible bean should not have been processed by hidden BPP", beansSeen.contains("visible"));
		Assert.assertTrue("bpp did not process hidden bean", beansSeen.contains("hidden"));
		Assert.assertEquals("setByBPP", ctx.getBean("hiddenName"));
	}
	public static class HiddenBPPWithReferenceFromVisibleBean {
		public @Bean String hiddenName() {
			return hidden().getName();
		}

		public @Bean TestBean visible() {
			return new TestBean("originalVisibleName");
		}

		@Bean TestBean hidden() {
			return new TestBean("originalHiddenName");
		}

		// hidden bean post processor should apply to hidden beans
		@Bean TrackingBPP trackingBpp() { return new TrackingBPP(); }
	}


	public @Test void visibleBeanPostProcessorIsAppliedToHiddenBeansEvenWhenVisibleBeanAccessesHiddenBean() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(VisibleBPPWithReferenceFromVisibleBean.class);

		List<String> beansSeen = TrackingBPP.beansSeen;
		Assert.assertFalse("no beans were post processed", beansSeen.isEmpty());
		Assert.assertTrue("bpp did not process visible bean", beansSeen.contains("visible"));
		Assert.assertTrue("bpp did not process hidden bean", beansSeen.contains("hidden"));
		Assert.assertEquals("setByBPP", ctx.getBean("hiddenName"));
	}
	public static class VisibleBPPWithReferenceFromVisibleBean {
		public @Bean String hiddenName() {
			return hidden().getName();
		}

		public @Bean TestBean visible() {
			return new TestBean("originalVisibleName");
		}

		@Bean TestBean hidden() {
			return new TestBean("originalHiddenName");
		}

		public @Bean TrackingBPP trackingBpp() { return new TrackingBPP(); }
	}


	public @Test void visibleBeanPostProcessorIsAppliedToBothVisibleAndHiddenBeans() {
		JavaConfigApplicationContext ctx = new JavaConfigApplicationContext(VisibleBPPWithBothHiddenAndVisibleBeans.class);

		List<String> beansSeen = TrackingBPP.beansSeen;
		Assert.assertFalse("no beans were post processed", beansSeen.isEmpty());
		Assert.assertTrue("bpp did not process visible bean", beansSeen.contains("visible"));
		Assert.assertTrue("bpp did not process hidden bean", beansSeen.contains("hidden"));
		Assert.assertEquals("setByBPP", ctx.getBean("hiddenName"));
	}
	public static class VisibleBPPWithBothHiddenAndVisibleBeans {
		public @Bean String hiddenName() {
			return hidden().getName();
		}

		public @Bean TestBean visible() {
			return new TestBean("originalVisibleName");
		}

		@Bean TestBean hidden() {
			return new TestBean("originalHiddenName");
		}

		public @Bean TrackingBPP trackingBpp() {
			return new TrackingBPP();
		}
	}


}

class TrackingBPP implements BeanPostProcessor {

	static ArrayList<String> beansSeen = new ArrayList<String>();

	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		beansSeen.add(beanName);
		if(bean instanceof TestBean) {
			String newName = "setByBPP";
			((TestBean)bean).setName(newName);
		}
		return bean;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
}
