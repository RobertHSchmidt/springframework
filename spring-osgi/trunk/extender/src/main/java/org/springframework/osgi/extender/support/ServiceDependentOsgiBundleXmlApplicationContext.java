/*
 * Copyright 2002-2006 the original author or authors.
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
 *
 */
package org.springframework.osgi.extender.support;

import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;

/**
 * An OsgiBundleXmlApplicationContext which delays initialization until all
 * of its osgi:reference targets are complete. <p/>
 *
 * @author Andy Piper
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class ServiceDependentOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {
    protected volatile ContextState state = ContextState.INITIALIZED;
    protected DependencyListener listener;


    public ServiceDependentOsgiBundleXmlApplicationContext(BundleContext context, String[] configLocations) {
        super(context, configLocations);
    }

    protected synchronized void create(final Runnable postAction) throws BeansException {
        preRefresh();
        DependencyListener dl = new DependencyListener(postAction, this);
        dl.findServiceDependencies();

        state = ContextState.RESOLVING_DEPENDENCIES;
        if (!dl.isSatisfied()) {
            listener = dl;
            listener.register();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No outstanding dependencies, completing initialization for "
                          + getDisplayName());
            } 
            postAction.run();
        }
    }

    public synchronized void close() {
        state = ContextState.CLOSED;
        if (listener != null) {
            listener.deregister();
        }
        super.close();
    }


    protected void interrupt() {
        state = ContextState.INTERRUPTED;
        if (listener != null) {
            listener.deregister();
        }
    }

    protected void complete() {
        postRefresh();
    }


    public boolean isAvailable() {
        return isActive() && (state == ContextState.DEPENDENCIES_RESOLVED || state == ContextState.CREATED);
    }


    public ContextState getState() {
        return state;
    }

    protected void setState(ContextState s) {
        state = s;
    }


    public DependencyListener getListener() {
        return listener;
    }
}
