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
package org.apache.myfaces.mvc.impl.view.facelet.tag.fn;

import java.io.IOException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIOutcomeTarget;
import javax.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletFunction;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared.renderkit.html.util.OutcomeTargetUtils;

/**
 *
 */
public final class FaceletsFunctionLibrary
{
    
    public FaceletsFunctionLibrary()
    {
        super();
    }
    
    @JSFFaceletFunction(name="ma:getLink")
    public static String getLink(String outcome)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIOutcomeTarget target = (UIOutcomeTarget) facesContext.getApplication().createComponent(
                facesContext, "org.apache.myfaces.mvc.DefineLinkComponent", null);
        
        if (outcome != null && outcome.length() > 0)
        {
            target.setOutcome(outcome);
        }
        
        if(target != null)
        {
            try
            {
                String targetHref = OutcomeTargetUtils.getOutcomeTargetHref(facesContext, target);

                return targetHref;
            }
            catch (IOException ex)
            {
            }
        }
        return null;
    }
   
    @JSFFaceletFunction(name="ma:getLinkFrom")
    public static String getLinkFrom(String id)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent reference = UIComponent.getCurrentComponent(facesContext);
        if (reference == null)
        {
            reference = UIComponent.getCurrentCompositeComponent(facesContext);
        }
        if (reference == null)
        {
            reference = facesContext.getViewRoot();
        }
        
        UIOutcomeTarget target = (UIOutcomeTarget) reference.findComponent(id);
        
        if(target != null)
        {
            try
            {
                String targetHref = OutcomeTargetUtils.getOutcomeTargetHref(facesContext, target);

                return targetHref;
            }
            catch (IOException ex)
            {
            }
        }
        return null;
    }

    @JSFFaceletFunction(name="ma:encodeActionURL")
    public static String encodeActionURL()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        return facesContext.getExternalContext().encodeActionURL(
                viewHandler.getActionURL(facesContext, viewId));
    }
    
    @JSFFaceletFunction(name="ma:sourceActionURL")
    public static String sourceActionURL(String id)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent reference = UIComponent.getCurrentComponent(facesContext);
        if (reference == null)
        {
            reference = UIComponent.getCurrentCompositeComponent(facesContext);
        }
        if (reference == null)
        {
            reference = facesContext.getViewRoot();
        }
        
        UICommand target = (UICommand) reference.findComponent(id);
        
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String viewId = facesContext.getViewRoot().getViewId();
        //Map params = new HashMap();
        //params.
        String actionURL = viewHandler.getActionURL(facesContext, viewId);
        
        actionURL = actionURL + "?oamva="+target.getClientId();
        
        return facesContext.getExternalContext().encodeActionURL(actionURL);
    }

    @JSFFaceletFunction(name="ma:clientId")
    public static String clientId(String id)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent reference = UIComponent.getCurrentComponent(facesContext);
        if (reference == null)
        {
            reference = UIComponent.getCurrentCompositeComponent(facesContext);
        }
        if (reference == null)
        {
            reference = facesContext.getViewRoot();
        }
        return reference.findComponent(id).getClientId(facesContext);
    }
    
    @JSFFaceletFunction(name="ma:parentFormId")
    public static String parentFormId()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent reference = UIComponent.getCurrentComponent(facesContext);
        if (reference == null)
        {
            reference = UIComponent.getCurrentCompositeComponent(facesContext);
        }
        if (reference == null)
        {
            reference = facesContext.getViewRoot();
        }
        if (reference instanceof UIForm)
        {
            return reference.getClientId(facesContext);
        }
        FormInfo info = RendererUtils.findNestingForm(reference, facesContext);
        return info.getForm().getClientId(facesContext);
    }
}
