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

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.mvc.impl.cdi.ActionControllerHolderImpl;

/**
 *
 * @author lu4242
 */
public class LifecycleWrapperImpl extends LifecycleWrapper
{
    private final Log log = LogFactory.getLog(LifecycleWrapperImpl.class);

    private Lifecycle delegate;
    
    private ValueExpression actionControllerVE;

    public LifecycleWrapperImpl(Lifecycle delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void execute(FacesContext context) throws FacesException
    {
        ActionControllerHolderImpl holder = context.getApplication().evaluateExpressionGet(context,
                "#{oam_mvc_ACTION_CONTROLLER}", ActionControllerHolderImpl.class);
        if (holder != null)
        {
            holder.processActions(context);
        }
        super.execute(context);
    }

    @Override
    public Lifecycle getWrapped()
    {
        return delegate;
    }
    
}
