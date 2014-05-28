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
package org.apache.myfaces.mvc.api.component;

import java.util.Map;
import javax.faces.component.UIViewAction;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.mvc.api.model.ViewParamModel;

/**
 *
 * @author lu4242
 */
@JSFComponent(type = "oam.mvc.ViewAction")
public class ViewActionComponent extends UIViewAction
{
    
    public static final String COMPONENT_TYPE = "oam.mvc.ViewAction";
    
    @Override
    public void decode(FacesContext context)
    {
        // Check if the action should be processed only on postback
        if (isProcessOnPostback() && !context.isPostback())
        {
            // do not decode
            return;
        }
        
        ViewParamModel[] params = getParams();
        if (params != null && params.length > 0)
        {
            boolean valid = true;
            Map<String, String> paramMap = context.getExternalContext()
                    .getRequestParameterMap();   

            // Only decode the action if 
            // TODO: pattern matching it is possible here
            for (ViewParamModel param : params)
            {
                if (!paramMap.containsKey(param.getName()))
                {
                    valid = false;
                    break;
                }
                else if (param.isCheckEquals() && 
                        (!param.getValue().equals(paramMap.get(param.getName())) ) )
                {
                    valid = false;
                    break;
                }
                
                /*
                int equalsIndex = param.indexOf('=');
                if (equalsIndex != -1)
                {
                    String name = param.substring(0, equalsIndex);
                    if (!paramMap.containsKey(name))
                    {
                        valid = false;
                        break;
                    }                    
                }
                else
                {
                    if (!paramMap.containsKey(param))
                    {
                        valid = false;
                        break;
                    }
                }*/
            }
            if (valid)
            {
                setSubmitted(true);
            }
        }
        else
        {
            setSubmitted(true);
        }
        
        if (!isSubmitted())
        {
            return;
        }
        
        super.decode(context);
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        if (!isSubmitted())
        {
            return;
        }
        
        super.broadcast(event);
    }
    
    public ViewParamModel[] getParams()
    {
        return (ViewParamModel[]) getStateHelper().get(PropertyKeys.params);
    }
    
    public void setParams(ViewParamModel[] event)
    {
        getStateHelper().put(PropertyKeys.params, event);
    }
    
    public boolean isSubmitted()
    {
        return (Boolean) getTransientStateHelper().getTransient(PropertyKeys.submitted, Boolean.FALSE);
    }

    public void setSubmitted(boolean submitted)
    {
        getTransientStateHelper().putTransient(PropertyKeys.submitted, submitted);
    }
    
    public boolean isProcessOnPostback()
    {
        return (Boolean) getStateHelper().eval(PropertyKeys.processOnPostback, Boolean.FALSE);
    }
    
    public void setProcessOnPostback(boolean processOnPostback)
    {
        getStateHelper().put(PropertyKeys.processOnPostback, processOnPostback);
    }
    
    protected enum PropertyKeys
    {
        params,
        processOnPostback,
        submitted
    }
}
