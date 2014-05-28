/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.mvc.impl.lifecycle;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;

/**
 *
 */
public class LifecycleWrapperFactoryImpl extends LifecycleFactory
{
    private LifecycleFactory delegate;
    
    private final Map<String, Lifecycle> _lifecycles = new ConcurrentHashMap<String, Lifecycle>();

    public LifecycleWrapperFactoryImpl(LifecycleFactory delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void addLifecycle(String lifecycleId, Lifecycle lifecycle)
    {
        Lifecycle o = new LifecycleWrapperImpl(lifecycle);
        _lifecycles.put(lifecycleId, o);
    }

    @Override
    public Lifecycle getLifecycle(String lifecycleId)
    {
        Lifecycle lifecycle = _lifecycles.get(this);
        if (lifecycle == null)
        {
            lifecycle = new LifecycleWrapperImpl(delegate.getLifecycle(lifecycleId));
            _lifecycles.put(lifecycleId, lifecycle);
        }
        return lifecycle;
    }

    @Override
    public Iterator<String> getLifecycleIds()
    {
        return delegate.getLifecycleIds();
    }
}
