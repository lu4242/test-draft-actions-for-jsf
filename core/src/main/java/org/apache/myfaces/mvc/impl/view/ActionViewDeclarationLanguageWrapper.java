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
package org.apache.myfaces.mvc.impl.view;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;
import org.apache.myfaces.mvc.impl.cdi.ActionControllerHolderImpl;

/**
 *
 * @author lu4242
 */
public class ActionViewDeclarationLanguageWrapper extends ViewDeclarationLanguageWrapper
{
    
    private ViewDeclarationLanguage delegate;
    
    private boolean _initialized = false;
    private Pattern _acceptPatterns;
    private String _extension;
    
    private ActionControllerHolderImpl actionControllerHolder;
    
    private final Map<String, Boolean> actionViews = new ConcurrentHashMap<String, Boolean>();

    public ActionViewDeclarationLanguageWrapper(ViewDeclarationLanguage delegate)
    {
        this.delegate = delegate;
    }
    
    public void initPatterns(FacesContext facesContext)
    {
        if (!_initialized)
        {
            ExternalContext eContext = facesContext.getExternalContext();
            _acceptPatterns = loadAcceptPattern(eContext);
            _extension = loadFaceletExtension(eContext);
            _initialized = true;
        }
    }

    @Override
    public boolean viewExists(FacesContext facesContext, String viewId)
    {
        initPatterns(facesContext);
        boolean viewExists = super.viewExists(facesContext, viewId);
        if (!viewExists && handles(viewId))
        {
            // Check if the view exists implicitly from declaration in a bean.
            boolean viewExistsInAnnotation = getActionControllerHolder(facesContext).viewExists(facesContext, viewId);
            if (viewExistsInAnnotation)
            {
                actionViews.put(viewId, viewExists);
                viewExists = viewExistsInAnnotation;
            }
        }
        return viewExists;
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot view) throws IOException
    {
        if (getActionControllerHolder(context).viewExists(context, view.getViewId()))
        {
            String viewId = view.getViewId();
            if (viewGeneratedByAnnotation(context, viewId))
            {
                // The view only exists in the annotation, skip build view.
                return;
            }
        }
        super.buildView(context, view);
    }
    
    private boolean viewGeneratedByAnnotation(FacesContext context, String viewId)
    {
        if (!actionViews.containsKey(viewId))
        {
            actionViews.put(viewId, super.viewExists(context, viewId));
        }
        return (Boolean.FALSE.equals(actionViews.get(viewId)));
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId)
    {
        if (viewGeneratedByAnnotation(context, viewId))
        {
            final String fviewId = viewId;
            return new ActionViewMetadata(new ViewMetadata()
            {

                @Override
                public UIViewRoot createMetadataView(FacesContext fc)
                {
                    return fc.getApplication().getViewHandler().createView(fc, fviewId);
                }

                @Override
                public String getViewId()
                {
                    return fviewId;
                }
            });
        }
        else
        {
            return new ActionViewMetadata(super.getViewMetadata(context, viewId));
        }
    }
    
    @Override
    public ViewDeclarationLanguage getWrapped()
    {
        return delegate;
    }
    
    private ActionControllerHolderImpl getActionControllerHolder(FacesContext facesContext)
    {
        if (actionControllerHolder == null)
        {
            actionControllerHolder = ActionControllerHolderImpl.getActionControllerHolder(facesContext);
        }
        return actionControllerHolder;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean handles(String viewId)
    {
        if (viewId == null)
        {
            return false;
        }
        // Check extension first as it's faster than mappings
        if (viewId.endsWith(_extension))
        {
            // If the extension matches, it's a Facelet viewId.
            return true;
        }

        // Otherwise, try to match the view identifier with the facelet mappings
        return _acceptPatterns != null && _acceptPatterns.matcher(viewId).matches();
    }    
    
    /**
     * Load and compile a regular expression pattern built from the Facelet view mapping parameters.
     * 
     * @param context
     *            the application's external context
     * 
     * @return the compiled regular expression
     */
    private Pattern loadAcceptPattern(ExternalContext context)
    {
        assert context != null;

        String mappings = context.getInitParameter(ViewHandler.FACELETS_VIEW_MAPPINGS_PARAM_NAME);
        if(mappings == null)    //consider alias facelets.VIEW_MAPPINGS
        {
            mappings = context.getInitParameter("facelets.VIEW_MAPPINGS");
        }
        if (mappings == null)
        {
            return null;
        }

        // Make sure the mappings contain something
        mappings = mappings.trim();
        if (mappings.length() == 0)
        {
            return null;
        }

        return Pattern.compile(toRegex(mappings));
    }

    private String loadFaceletExtension(ExternalContext context)
    {
        assert context != null;

        String suffix = context.getInitParameter(ViewHandler.FACELETS_SUFFIX_PARAM_NAME);
        if (suffix == null)
        {
            suffix = ViewHandler.DEFAULT_FACELETS_SUFFIX;
        }
        else
        {
            suffix = suffix.trim();
            if (suffix.length() == 0)
            {
                suffix = ViewHandler.DEFAULT_FACELETS_SUFFIX;
            }
        }

        return suffix;
    }

    /**
     * Convert the specified mapping string to an equivalent regular expression.
     * 
     * @param mappings
     *            le mapping string
     * 
     * @return an uncompiled regular expression representing the mappings
     */
    private String toRegex(String mappings)
    {
        assert mappings != null;

        // Get rid of spaces
        mappings = mappings.replaceAll("\\s", "");

        // Escape '.'
        mappings = mappings.replaceAll("\\.", "\\\\.");

        // Change '*' to '.*' to represent any match
        mappings = mappings.replaceAll("\\*", ".*");

        // Split the mappings by changing ';' to '|'
        mappings = mappings.replaceAll(";", "|");

        return mappings;
    }
}
