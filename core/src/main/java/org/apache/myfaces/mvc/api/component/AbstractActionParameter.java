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

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.shared.util.MessageUtils;

/**
 *
 */
@JSFComponent(name = "ma:actionParam",
              clazz =  "org.apache.myfaces.mvc.api.component.ActionParameter")
public class AbstractActionParameter extends UIInput
{
    public static final String COMPONENT_FAMILY = "org.apache.myfaces.mvc.ActionParameter";
    public static final String COMPONENT_TYPE = "org.apache.myfaces.mvc.ActionParameter";
    
    public AbstractActionParameter()
    {
        setRendererType(null);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    @Override
    public void decode(FacesContext context)
    {
        // Override behavior from superclass to pull a value from the incoming request parameter map under the 
        // name given by getName() and store it with a call to UIInput.setSubmittedValue(java.lang.Object).
        String value = context.getExternalContext().getRequestParameterMap().get(getName());
        
        // only apply the value if it is non-null (otherwise postbacks 
        // to a view with view parameters would not work correctly)
        if (value != null)
        {
            setSubmittedValue(value);
        }
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException
    {
        if (context == null) 
        {
            throw new NullPointerException();
        }
        setSubmittedValue(getStringValue(context));
    }

    @JSFProperty(required = true)
    public String getName()
    {
        return (String) getStateHelper().eval(PropertyKeys.name);
    }

    public String getStringValue(FacesContext context)
    {
        if (getValueExpression ("value") != null) 
        {
            // Value specified as an expression, so do the conversion.
            
            return getStringValueFromModel (context);
        }
        
        // Otherwise, just return the local value.
        
        return ((String) this.getLocalValue());
    }

    public String getStringValueFromModel(FacesContext context) throws ConverterException
    {
        ValueExpression ve = getValueExpression ("value");
        Converter converter;
        Object value;
        
        if (ve == null) 
        {
            // No value expression, return null.
            return null;
        }
        
        value = ve.getValue (context.getELContext());
        
        if (value instanceof String) 
        {
            // No need to convert.
            return ((String) value);
        }
        
        converter = getConverter();
        
        if (converter == null) 
        {
            if (value == null) 
            {
                // No converter, no value, return null.
                return null;
            }
            
            // See if we can create the converter from the value type.
            
            converter = context.getApplication().createConverter (value.getClass());
            
            if (converter == null) 
            {
                // Only option is to call toString().
                
                return value.toString();
            }
        }
        
        return converter.getAsString (context, this, value);
    }

    @JSFProperty(tagExcluded=true)
    @Override
    public boolean isImmediate()
    {
        return false;
    }
    
    @JSFProperty(tagExcluded=true)
    @Override
    public boolean isRendered()
    {
        return super.isRendered();
    }
    
    @Override
    public void processValidators(FacesContext context)
    {
        if (context == null) 
        {
            throw new NullPointerException ("context");
        }
        
        // If value is null and required is set, validation fails.
        
        if ((getSubmittedValue() == null) && isRequired()) 
        {
            FacesMessage message;
            String required = getRequiredMessage();
            
            if (required != null) 
            {
                message = new FacesMessage (FacesMessage.SEVERITY_ERROR, required, required);
            }
            else 
            {
                Object label = MessageUtils.getLabel (context, this);
                
                message = MessageUtils.getMessage (
                     FacesMessage.SEVERITY_ERROR, REQUIRED_MESSAGE_ID, new Object[] { label });
            }
            
            setValid (false);
            
            context.addMessage (getClientId (context), message);
            context.validationFailed();
            context.renderResponse();
            
            return;
        }
        
        super.processValidators (context);
    }
    
    enum PropertyKeys
    {
        name
    }
    
    public void setName(String name)
    {
        getStateHelper().put(PropertyKeys.name, name );
    }

    @Override
    public void updateModel(FacesContext context)
    {
        super.updateModel(context);
        
        // Put name in request map if value is not a value expression, is valid, and local
        // value was set.
        
        if ((getValueExpression ("value") == null) && isValid() && isLocalValueSet()) 
        {
            context.getExternalContext().getRequestMap().put (getName(), getLocalValue());
        }
    }
    

}
