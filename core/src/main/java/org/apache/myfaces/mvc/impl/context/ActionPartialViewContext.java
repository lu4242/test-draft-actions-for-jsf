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
package org.apache.myfaces.mvc.impl.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.faces.FactoryFinder;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitContextFactory;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextWrapper;
import javax.faces.event.PhaseId;
import org.apache.myfaces.mvc.api.component.AbstractDefineActionComponent;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.util.FormInfo;

/**
 *
 */
public class ActionPartialViewContext extends PartialViewContextWrapper
{
    private static final String ACTION_REQUEST = "oamva";
    
    private PartialViewContext delegate;

    private Boolean _partialRequest = null;
    
    private Boolean _actionRequest = null;
    
    private Collection<String> _executeClientIds = null;    
    
    private FacesContext _facesContext = null;
    private VisitContextFactory _visitContextFactory = null;
    
    private static final  Set<VisitHint> PARTIAL_EXECUTE_HINTS = Collections.unmodifiableSet( 
            EnumSet.of(VisitHint.EXECUTE_LIFECYCLE, VisitHint.SKIP_UNRENDERED));

    public ActionPartialViewContext(PartialViewContext delegate)
    {
        this.delegate = delegate;
        _facesContext = FacesContext.getCurrentInstance();
    }

    @Override
    public PartialViewContext getWrapped()
    {
        return delegate;
    }

    public boolean isActionRequest()
    {
        if (_actionRequest == null)
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String actionRequestTarget = facesContext.getExternalContext().
                    getRequestParameterMap().get(ACTION_REQUEST);
            _actionRequest = (actionRequestTarget != null && actionRequestTarget.length() > 0);
        }
        return _actionRequest;
    }
    
    @Override
    public boolean isPartialRequest()
    {
        if (_partialRequest == null)
        {
            boolean containsAction = isActionRequest();
            if (containsAction)
            {
                _partialRequest = containsAction;
            }
        }
        if (_partialRequest == null)
        {
            _partialRequest = super.isPartialRequest();
        }
        return _partialRequest;
    }
    
    @Override
    public Collection<String> getExecuteIds()
    {
        if (_executeClientIds == null)
        {
            _executeClientIds = super.getExecuteIds();
            boolean containsAction = isActionRequest();
            if (containsAction)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                String actionRequestTarget = facesContext.getExternalContext().
                        getRequestParameterMap().get(ACTION_REQUEST);
                _executeClientIds.add(actionRequestTarget);
            }
        }
        return _executeClientIds;
    }

    @Override
    public void processPartial(PhaseId phaseId)
    {
        if (!isActionRequest())
        {
            super.processPartial(phaseId);
        }
        else
        {
            // Process partial action request
            final UIViewRoot viewRoot = _facesContext.getViewRoot();
            final Collection<String> executeIds = getExecuteIds();
            if (executeIds == null || executeIds.isEmpty())
            {
                return;
            }
            
            final PhaseId phaseIdToGo = phaseId;
            // Now we should calculate the list of ids to be refreshed, according to the action invoked.
            for (String id : executeIds)
            {
                viewRoot.invokeOnComponent(_facesContext, id, new ContextCallback()
                {
                    public void invokeContextCallback(FacesContext fc, UIComponent target)
                    {
                        List<String> ids = new ArrayList<String>(executeIds);
                        if (target instanceof AbstractDefineActionComponent)
                        {
                            ids.clear();
                            ids.addAll(((AbstractDefineActionComponent)target).getExecuteList());
                        }
                        for (int i = 0; i < ids.size(); i++)
                        {
                            String id = ids.get(i);
                            if ("@form".equals(id))
                            {
                                FormInfo formInfo = RendererUtils.findNestingForm(target, fc);
                                ids.set(i, formInfo.getForm().getClientId(fc));
                            }
                        }
                        
                        VisitContext visitCtx = getVisitContextFactory().getVisitContext(_facesContext, ids, 
                                PARTIAL_EXECUTE_HINTS);
                        viewRoot.visitTree(visitCtx, new PhaseAwareVisitCallback(_facesContext, phaseIdToGo));
                    }
                });
            }
            
            //VisitContext visitCtx = getVisitContextFactory().getVisitContext(_facesContext, executeIds, 
            //        PARTIAL_EXECUTE_HINTS);
            //viewRoot.visitTree(visitCtx, new PhaseAwareVisitCallback(_facesContext, phaseId));
        }
    }

    @Override
    public void setPartialRequest(boolean bln)
    {
        getWrapped().setPartialRequest(bln);
    }
    
    private VisitContextFactory getVisitContextFactory()
    {
        if (_visitContextFactory == null)
        {
            _visitContextFactory = (VisitContextFactory)FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY);
        }
        return _visitContextFactory;
    }
    
    private class PhaseAwareVisitCallback implements VisitCallback
    {

        private PhaseId _phaseId;
        private FacesContext _facesContext;

        public PhaseAwareVisitCallback(FacesContext facesContext, PhaseId phaseId)
        {
            this._phaseId = phaseId;
            this._facesContext = facesContext;
        }

        public VisitResult visit(VisitContext context, UIComponent target)
        {

            
            if (_phaseId == PhaseId.APPLY_REQUEST_VALUES)
            {
                target.processDecodes(_facesContext);
            }
            else if (_phaseId == PhaseId.PROCESS_VALIDATIONS)
            {
                target.processValidators(_facesContext);
            }
            else if (_phaseId == PhaseId.UPDATE_MODEL_VALUES)
            {
                target.processUpdates(_facesContext);
            }

            // Return VisitResult.REJECT as processDecodes/Validators/Updates already traverse sub tree
            return VisitResult.REJECT;
        }    
    }
}
