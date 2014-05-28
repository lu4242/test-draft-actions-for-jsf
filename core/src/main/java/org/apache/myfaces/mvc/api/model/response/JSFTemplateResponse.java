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
package org.apache.myfaces.mvc.api.model.response;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.StateManager;
import javax.faces.application.StateManagerWrapper;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.view.ViewDeclarationLanguage;
import org.apache.myfaces.mvc.impl.component.TemplateUIViewRoot;

/**
 * In this case JSF is used as a template engine to generate html content using the existing VDL.
 * No lifecycle decode/validation or state saving is performed.
 */
public class JSFTemplateResponse extends ActionResponse
{
    private String viewId;

    public JSFTemplateResponse(String viewId)
    {
        this.viewId = viewId;
    }

    @Override
    public void encodeBegin(FacesContext facesContext) throws IOException
    {
    }

    @Override
    public void encodeContent(FacesContext facesContext) throws IOException
    {
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        Application application = facesContext.getApplication();
        JSFTemplateFacesContextWrapper wrappedFacesContext = new JSFTemplateFacesContextWrapper(facesContext);
        try
        {
            wrappedFacesContext.setWrapperAsCurrentFacesContext();
            
            // Step 1: create the view
            UIViewRoot root = (UIViewRoot)
                    wrappedFacesContext.getApplication().getViewHandler().createView(
                    wrappedFacesContext, getViewId());
            if (root instanceof TemplateUIViewRoot)
            {
                ((TemplateUIViewRoot)root).setParentViewRoot(facesContext.getViewRoot());
            }
            wrappedFacesContext.setViewRoot(root);
            
            // Step 2: render the view
            ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(wrappedFacesContext, getViewId());
            vdl.buildView(wrappedFacesContext, root);
            
            application.publishEvent(facesContext, PreRenderViewEvent.class, root);
            
            viewHandler.renderView(facesContext, root);
        }
        finally
        {
            wrappedFacesContext.restoreCurrentFacesContext();
        }        
    }

    @Override
    public void encodeEnd(FacesContext facesContext) throws IOException
    {
        facesContext.responseComplete();
    }

    public String getViewId()
    {
        return viewId;
    }

    public void setViewId(String viewId)
    {
        this.viewId = viewId;
    }
    
    private static class JSFTemplateFacesContextWrapper extends FacesContextWrapper 
    {
        private FacesContext delegate;
        private UIViewRoot root;
        private Application application;

        public JSFTemplateFacesContextWrapper(FacesContext delegate)
        {
            this.delegate = delegate;
            this.application = new JSFTemplateApplication(this.delegate.getApplication());
        }

        @Override
        public void setViewRoot(UIViewRoot root)
        {
            this.root = root;
        }

        @Override
        public UIViewRoot getViewRoot()
        {
            return root;
        }

        @Override
        public Application getApplication()
        {
            return application;
        }

        @Override
        public FacesContext getWrapped()
        {
            return delegate;
        }
        
        void setWrapperAsCurrentFacesContext()
        {
            setCurrentInstance(this);
        }
        
        void restoreCurrentFacesContext()
        {
            setCurrentInstance(delegate);
        }
    }
    
    private static class JSFTemplateApplication extends ApplicationWrapper
    {
        private static StateManager DUMMY_STATE_MANAGER = new DummyStateManager();
        private Application delegate;

        public JSFTemplateApplication(Application delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public StateManager getStateManager()
        {
            return DUMMY_STATE_MANAGER;
        }
        
        @Override
        public UIComponent createComponent(String componentType) throws FacesException
        {
            if (UIViewRoot.COMPONENT_TYPE.equals(componentType))
            {
                return super.createComponent("oam.mvc.UIViewRoot");
            }
            return super.createComponent(componentType);
        }

        @Override
        public UIComponent createComponent(FacesContext context, String componentType, String rendererType)
        {
            if (UIViewRoot.COMPONENT_TYPE.equals(componentType))
            {
                return super.createComponent(context, "oam.mvc.UIViewRoot", rendererType);
            }
            return super.createComponent(context, componentType, rendererType);
        }
        
        @Override
        public Application getWrapped()
        {
            return delegate;
        }
    }
    
    private static class DummyStateManager extends StateManagerWrapper
    {

        @Override
        public UIViewRoot restoreView(FacesContext fc, String string, String string1)
        {
            return null;
        }

        @Override
        public boolean isSavingStateInClient(FacesContext context)
        {
            return true;
        }

        @Override
        protected void restoreComponentState(FacesContext context, UIViewRoot viewRoot, String renderKitId)
        {
        }

        @Override
        protected UIViewRoot restoreTreeStructure(FacesContext context, String viewId, String renderKitId)
        {
            return null;
        }

        @Override
        public String getViewState(FacesContext context)
        {
            return "";
        }

        @Override
        public void writeState(FacesContext context, Object state) throws IOException
        {
        }

        @Override
        public void writeState(FacesContext context, SerializedView state) throws IOException
        {
        }

        @Override
        protected Object getComponentStateToSave(FacesContext context)
        {
            return null;
        }

        @Override
        protected Object getTreeStructureToSave(FacesContext context)
        {
            return null;
        }

        @Override
        public Object saveView(FacesContext context)
        {
            return null;
        }

        @Override
        public SerializedView saveSerializedView(FacesContext context)
        {
            return null;
        }

        @Override
        public StateManager getWrapped()
        {
            return new StateManager()
            {

                @Override
                public UIViewRoot restoreView(FacesContext fc, String string, String string1)
                {
                    return null;
                }
            };
        }
    }
}
