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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreValidateEvent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 *
 */
@JSFComponent(name = "ma:defineAction",
              clazz =  "org.apache.myfaces.mvc.api.component.DefineActionComponent")
public class AbstractDefineActionComponent extends UICommand
{
    
    public static final String COMPONENT_TYPE = "org.apache.myfaces.mvc.DefineActionComponent";
    
    private static final String VAL_FORM = "@form";
    private static final String VAL_ALL = "@all";
    private static final String VAL_THIS = "@this";
    private static final String VAL_NONE = "@none";
    
    private static final Collection<String> VAL_FORM_LIST = Collections.singletonList(VAL_FORM);
    private static final Collection<String> VAL_ALL_LIST = Collections.singletonList(VAL_ALL);
    private static final Collection<String> VAL_THIS_LIST = Collections.singletonList(VAL_THIS);
    private static final Collection<String> VAL_NONE_LIST = Collections.singletonList(VAL_NONE);
    
    public AbstractDefineActionComponent()
    {
        setRendererType(null);
    }
    
    @Override
    public void decode(FacesContext facesContext)
    {
        AbstractDefineActionComponent component = this;
        Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
        String submittedValue = (String)paramMap.get("oamva");
        if (submittedValue != null && component.getClientId(facesContext).equals(submittedValue))
        {
            component.queueEvent(new ActionEvent(component));
            component.setSubmitted(true);
        }
        else
        {
            component.setSubmitted(false);
        }
    }
    
    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        if (!isSubmitted())
        {
            return;
        }
        
        super.broadcast(event);
        
        if (event instanceof ActionEvent)
        {
            getFacesContext().responseComplete();
        }
    }
    
    @Override
    public void processDecodes(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        try
        {
            pushComponentToEL(context, this);

            decode(context);

            if (!isSubmitted())
            {
                return;
            }

            int facetCount = getFacetCount();
            if (facetCount > 0)
            {
                for (UIComponent facet : getFacets().values())
                {
                    facet.processDecodes(context);
                }
            }

            for (int i = 0, childCount = getChildCount(); i < childCount; i++)
            {
                UIComponent child = getChildren().get(i);
                child.processDecodes(context);
            }

        }
        finally
        {
            popComponentFromEL(context);
        }
    }

    @Override
    public void processValidators(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        
        try
        {
            pushComponentToEL(context, this);
            // SF issue #1050022: a form used within a datatable will loose it's submitted state
            // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
            // to restore the submitted state we call decode here again
            if (!isSubmitted())
            {
                decode(context);
            }
            if (!isSubmitted())
            {
                return;
            }

            //Pre validation event dispatch for component
            context.getApplication().publishEvent(context,  PreValidateEvent.class, getClass(), this);

            int facetCount = getFacetCount();
            if (facetCount > 0)
            {
                for (UIComponent facet : getFacets().values())
                {
                    facet.processValidators(context);
                }
            }

            for (int i = 0, childCount = getChildCount(); i < childCount; i++)
            {
                UIComponent child = getChildren().get(i);
                child.processValidators(context);
            }

        }
        finally
        {
            context.getApplication().publishEvent(context,  PostValidateEvent.class, getClass(), this);
            popComponentFromEL(context);
        }
    }

    @Override
    public void processUpdates(FacesContext context)
    {
        if (context == null)
        {
            throw new NullPointerException("context");
        }
        
        try
        {
            pushComponentToEL(context, this);
            // SF issue #1050022: a form used within a datatable will loose it's submitted state
            // as UIForm is no EditableValueHolder and therefore it's state is not saved/restored by UIData
            // to restore the submitted state we call decode here again
            if (!isSubmitted())
            {
                decode(context);
            }
            if (!isSubmitted())
            {
                return;
            }

            int facetCount = getFacetCount();
            if (facetCount > 0)
            {
                for (UIComponent facet : getFacets().values())
                {
                    facet.processUpdates(context);
                }
            }

            for (int i = 0, childCount = getChildCount(); i < childCount; i++)
            {
                UIComponent child = getChildren().get(i);
                child.processUpdates(context);
            }

        }
        finally
        {
            popComponentFromEL(context);
        }
    }
    
    public boolean isSubmitted()
    {
        return (Boolean) getTransientStateHelper().getTransient(ViewActionComponent.PropertyKeys.submitted, Boolean.FALSE);
    }

    public void setSubmitted(boolean submitted)
    {
        getTransientStateHelper().putTransient(ViewActionComponent.PropertyKeys.submitted, submitted);
    }
    
    protected enum PropertyKeys
    {
        submitted,
        execute
    }
    
    public Collection<String> getExecuteList() 
    {
        // we have to evaluate the real value in this method,
        // because the value of the ValueExpression might
        // change (almost sure it does!)
        return evalForCollection(PropertyKeys.execute);
    }
    
    /**
     * Invokes eval on the getStateHelper() and tries to get a
     * Collection out of the result.
     * @param attributeName
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<String> evalForCollection(PropertyKeys attributeName)
    {
        Object value = getExecute();
        if (value == null)
        {
            return Collections.<String>emptyList();
        }
        else if (value instanceof Collection)
        {
            return (Collection<String>) value;
        }
        else if (value instanceof String)
        {
            return getCollectionFromSpaceSplitString((String) value);
        }
        else
        {
            throw new IllegalArgumentException("Type " + value.getClass()
                    + " not supported for attribute " + attributeName);
        }
    }
        
    /**
     * Splits the String based on spaces and returns the 
     * resulting Strings as Collection.
     * @param stringValue
     * @return
     */
    private Collection<String> getCollectionFromSpaceSplitString(String stringValue)
    {
        //@special handling for @all, @none, @form and @this
        if (stringValue.equals(VAL_FORM)) 
        {
            return VAL_FORM_LIST;
        } 
        else if (stringValue.equals(VAL_ALL)) 
        {
            return VAL_ALL_LIST;
        } 
        else if (stringValue.equals(VAL_NONE)) 
        {
            return VAL_NONE_LIST;
        } 
        else if (stringValue.equals(VAL_THIS)) 
        {
            return VAL_THIS_LIST; 
        }

        // not one of the "normal" values - split it and return the Collection
        String[] arrValue = stringValue.split(" ");
        return Arrays.asList(arrValue);
    }

    @JSFProperty
    public Object getExecute()
    {
        return getStateHelper().eval(PropertyKeys.execute);
    }
    
    public void setExecute(Object object)
    {
        getStateHelper().put(PropertyKeys.execute, object);
    }
}
