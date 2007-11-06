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
package org.springframework.osgi.context.support;

import org.osgi.framework.BundleContext;

/**
 * ThreadLocal management of the BundleContext.
 * <p/>
 * <strong>Note</strong>: this is a stateful component.
 *
 * @author Andy Piper
 * @author Costin Leau
 */
// FIXME: move this out of context.support pkg
public class LocalBundleContext {

    private final static InheritableThreadLocal contextLocal = new InheritableThreadLocal();

    /**
     * Get the local bundle's BundleContext.
     */
    public static BundleContext getContext() {
        return (BundleContext) contextLocal.get();
    }

    public static void setContext(BundleContext bundle) {
        contextLocal.set(bundle);
    }

    private LocalBundleContext() {
    }
}
