/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.transport.support;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultStrategiesHelperTest extends TestCase {

    public void testGetDefaultStrategies() throws Exception {

        Properties strategies = new Properties();
        strategies.put(Strategy.class.getName(),
                StrategyImpl.class.getName() + "," + ContextAwareStrategyImpl.class.getName());
        DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("strategy1", StrategyImpl.class);
        applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

        List result = helper.getDefaultStrategies(Strategy.class, applicationContext);
        assertNotNull("No result", result);
        assertEquals("Invalid amount of strategies", 2, result.size());
        assertTrue("Result not a Strategy implementation", result.get(0) instanceof Strategy);
        assertTrue("Result not a Strategy implementation", result.get(1) instanceof Strategy);
        assertTrue("Result not a StrategyImpl implementation", result.get(0) instanceof StrategyImpl);
        assertTrue("Result not a StrategyImpl implementation", result.get(1) instanceof ContextAwareStrategyImpl);
        ContextAwareStrategyImpl impl = (ContextAwareStrategyImpl) result.get(1);
        assertNotNull("No application context injected", impl.getApplicationContext());
    }

    public void testGetDefaultStrategy() throws Exception {
        Properties strategies = new Properties();
        strategies.put(Strategy.class.getName(), StrategyImpl.class.getName());
        DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("strategy1", StrategyImpl.class);
        applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

        Object result = helper.getDefaultStrategy(Strategy.class, applicationContext);
        assertNotNull("No result", result);
        assertTrue("Result not a Strategy implementation", result instanceof Strategy);
        assertTrue("Result not a StrategyImpl implementation", result instanceof StrategyImpl);
    }

    public void testGetDefaultStrategyMoreThanOne() throws Exception {
        Properties strategies = new Properties();
        strategies.put(Strategy.class.getName(),
                StrategyImpl.class.getName() + "," + ContextAwareStrategyImpl.class.getName());
        DefaultStrategiesHelper helper = new DefaultStrategiesHelper(strategies);

        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("strategy1", StrategyImpl.class);
        applicationContext.registerSingleton("strategy2", ContextAwareStrategyImpl.class);

        try {
            helper.getDefaultStrategy(Strategy.class, applicationContext);
            fail("Expected BeanInitializationException");
        }
        catch (BeanInitializationException ex) {
            // expected
        }
    }

    public void testResourceConstructor() throws Exception {
        Resource resource = new ClassPathResource("strategies.properties", getClass());
        new DefaultStrategiesHelper(resource);
    }

    public interface Strategy {

    }

    private static class StrategyImpl implements Strategy {

    }

    private static class ContextAwareStrategyImpl implements Strategy, ApplicationContextAware {

        private ApplicationContext applicationContext;

        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }
}