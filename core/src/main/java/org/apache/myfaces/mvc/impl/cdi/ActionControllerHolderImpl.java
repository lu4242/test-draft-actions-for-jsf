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
package org.apache.myfaces.mvc.impl.cdi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PreDestroy;
import javax.el.MethodExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewMetadata;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.myfaces.mvc.api.annotation.ViewAction;
import org.apache.myfaces.mvc.api.annotation.ViewParam;
import org.apache.myfaces.mvc.api.component.ViewActionComponent;
import org.apache.myfaces.mvc.api.model.ViewParamModel;
import org.apache.myfaces.mvc.api.model.response.ActionResponse;

/**
 *
 * @author lu4242
 */
@Named("oam_mvc_ACTION_CONTROLLER")
@ApplicationScoped
public class ActionControllerHolderImpl
{
    
    private final Map<Class, ActionControllerMetadata> actionControllers;
    private final Map<Class, ActionControllerMetadata> unmodifiableActionControllers;
    
    private final Map<String, Boolean> viewIds;
    
    @Inject
    @Any 
    Instance<Object> objectBuilderInstances;
    
    public ActionControllerHolderImpl()
    {
        actionControllers = new ConcurrentHashMap<Class, ActionControllerMetadata>();
        unmodifiableActionControllers = Collections.unmodifiableMap(actionControllers);
        viewIds = new ConcurrentHashMap<String, Boolean>();
    }
    
    public void addActionController(AnnotatedType type)
    {
        actionControllers.put(type.getJavaClass(), new ActionControllerMetadata(type));
    }
    
    /**
     * Do the necessary operations over the actionControllers to initialize them.
     */
    public void init()
    {
        // Consolidate the viewIds that are mentioned by the actionControllers in a single map.
        for (ActionControllerMetadata metadata : actionControllers.values())
        {
            if (!metadata.getViewActions().isEmpty())
            {
                for (AnnotatedMethod am : metadata.getViewActions())
                {
                    ViewAction va = am.getAnnotation(ViewAction.class);
                    String[] values = va.value();
                    for (String v : values)
                    {
                        if (!v.contains("*"))
                        {
                            //Exact case
                            viewIds.put(v, Boolean.TRUE);
                        }
                    }
                }
            }
        }
    }
    
    protected Map<Class, ActionControllerMetadata> getActionControllers()
    {
        return unmodifiableActionControllers;
    }
    
    @PreDestroy
    public void destroy()
    {
        actionControllers.clear();
    }
    
    public static ActionControllerHolderImpl getActionControllerHolder(FacesContext context)
    {
        return context.getApplication().evaluateExpressionGet(context,
                "#{oam_mvc_ACTION_CONTROLLER}", ActionControllerHolderImpl.class);
    }
    
    public void processActions(FacesContext context)
    {
        // 1. Derive the url without faces servlet mapping.
                
        // 2. Check if the request should be handled by an action controller
        
        // 3. Call in order of priority the action controllers.
        
        //System.out.println("Action Controller processed!");
    }
    
    public void processViewActions(FacesContext facesContext, UIViewRoot root)
    {
        List<AnnotatedMethod> amList = null;
        List<ActionControllerMetadata> acmList = null;
        
        for (ActionControllerMetadata metadata : actionControllers.values())
        {
            if (!metadata.getViewActions().isEmpty())
            {
                for (AnnotatedMethod am : metadata.getViewActions())
                {
                    ViewAction va = am.getAnnotation(ViewAction.class);
                    String[] values = va.value();
                    for (String v : values)
                    {
                        if (matchPattern(root.getViewId(), v))
                        {
                            if (amList == null)
                            {
                                amList = new ArrayList<AnnotatedMethod>();
                                acmList = new ArrayList<ActionControllerMetadata>();
                            }
                            amList.add(am);
                            acmList.add(metadata);
                            //System.out.println("Added View Action from annotations!");
                        }
                    }
                }
            }
        }
        
        if (amList != null)
        {
            if (amList.size() > 0)
            {
                //Sorting
                
            }
            
            UIComponent metadataFacet = root.getFacet(UIViewRoot.METADATA_FACET_NAME);

            if (metadataFacet == null)
            {
                metadataFacet = facesContext.getApplication().createComponent(
                            facesContext, UIPanel.COMPONENT_TYPE, null);
                root.getFacets().put(UIViewRoot.METADATA_FACET_NAME, metadataFacet);
            }
            
            Collection<UIViewParameter> parameters = ViewMetadata.getViewParameters(root);
            
            // Create a map with the existing viewParam components and fill it
            Map<String, UIViewParameter> viewParamsMap = new HashMap<String, UIViewParameter>();
            if (!parameters.isEmpty())
            {
                for (UIViewParameter parameter : parameters)
                {
                    viewParamsMap.put(parameter.getName(), parameter);
                }
            }
            
            // Create an structure to evaluate later how create in a implicit way f:viewParam components.
            Map<String, ViewParam> definedViewParamAnnotationsMap = new HashMap<String, ViewParam>();
            Map<String, AnnotatedParameter> usedAnnotatedViewParamMap = new HashMap<String, AnnotatedParameter>();
                        
            for (int i = 0; i < amList.size(); i++)
            {
                AnnotatedMethod am = amList.get(i);
                ActionControllerMetadata acm = acmList.get(i);
                
                //1. get action controller
                if (acm.getType().isAnnotationPresent(Named.class))
                {
                    Named namedAnno = acm.getType().getAnnotation(Named.class);
                    String name = namedAnno.value();

                    ViewActionComponent viewAction = (ViewActionComponent) 
                        facesContext.getApplication().createComponent(facesContext, 
                                ViewActionComponent.COMPONENT_TYPE, null);
                    viewAction.setId(root.createUniqueId(facesContext, null));
                    
                    ViewAction va = am.getAnnotation(ViewAction.class);
                    if (va.immediate())
                    {
                        viewAction.setImmediate(true);
                    }
                    if (va.onPostback())
                    {
                        viewAction.setOnPostback(true);
                    }
                    if (va.processOnPostback())
                    {
                        viewAction.setProcessOnPostback(true);
                        viewAction.setOnPostback(true);
                    }
                    if (va.phase() != null && va.phase().length() > 0)
                    {
                        viewAction.setPhase(va.phase());
                    }
                    Map<String, ViewParamModel> viewParamModelMap = new TreeMap<String, ViewParamModel>();

                    if (va.params() != null && va.params().length > 0)
                    {
                        for (int j = 0; j < va.params().length; j++)
                        {
                            ViewParam vp = va.params()[j];
                            viewParamModelMap.put(vp.value(), new ViewParamModel(
                                    vp.value(), vp.expectedValue(), !isStringEmpty(vp.expectedValue())));
                            definedViewParamAnnotationsMap.put(vp.value(), vp);
                        }
                    }

                    // Take the params from the method parameters
                    List<AnnotatedParameter> apList = am.getParameters();
                    for (int j = 0; j < apList.size(); j++ )
                    {
                        AnnotatedParameter ap = apList.get(j);
                        ViewParam vp = ap.getAnnotation(ViewParam.class);
                        if (vp != null)
                        {
                            viewParamModelMap.put(vp.value(), new ViewParamModel(
                                vp.value(), vp.expectedValue(), !isStringEmpty(vp.expectedValue())));
                            usedAnnotatedViewParamMap.put(vp.value(), ap);
                        }
                    }
                    
                    if (!viewParamModelMap.isEmpty())
                    {
                        viewAction.setParams(
                                viewParamModelMap.values().toArray(
                                        new ViewParamModel[viewParamModelMap.size()]));
                    }
                    
                    // Create the underyling method expression wrapper
                    
                    Class[] paramClasses = new Class[apList.size()];
                    for (int j = 0; j < apList.size(); j++)
                    {
                        paramClasses[j] = (Class) apList.get(j).getBaseType();
                    }
                    
                    MethodExpression expr = facesContext.getApplication().getExpressionFactory().
                            createMethodExpression(facesContext.getELContext(),
                                    "#{"+name+"."+am.getJavaMember().getName()+"}", am.getJavaMember().getReturnType(),
                                    paramClasses);
                    
                    viewAction.setActionExpression(new ViewActionMethodExpression(
                            acm.getType().getJavaClass(), expr, am.getJavaMember().getName(), paramClasses));
                    
                    //viewAction.addActionListener(new ViewActionActionListener(name, expr));
                    
                    metadataFacet.getChildren().add(viewAction);
                }
            }
            
            
            // check if all parameters are there, otherwise we need to create a couple of components:
            for (Map.Entry<String, AnnotatedParameter> entry : usedAnnotatedViewParamMap.entrySet())
            {
                if (!viewParamsMap.containsKey(entry.getKey()))
                {
                    UIViewParameter paramComponent = (UIViewParameter) 
                            facesContext.getApplication().createComponent(
                                facesContext, UIViewParameter.COMPONENT_TYPE, null);
                    paramComponent.setId(root.createUniqueId(facesContext, null));
                    
                    paramComponent.setName(entry.getKey());
                    
                    ViewParam vp = entry.getValue().getAnnotation(ViewParam.class);
                    
                    if (vp.converter() != null && vp.converter().length() > 0)
                    {
                        paramComponent.setConverter(facesContext.getApplication().createConverter(vp.converter()));
                    }
                    else if (!String.class.equals(entry.getValue().getBaseType()))
                    {
                        paramComponent.setConverter(facesContext.getApplication().createConverter(
                                (Class)entry.getValue().getBaseType()));
                    }

                    metadataFacet.getChildren().add(paramComponent);
                    viewParamsMap.put(paramComponent.getName(), paramComponent);
                }
            }
            
            for (Map.Entry<String, ViewParam> entry : definedViewParamAnnotationsMap.entrySet())
            {
                if (!viewParamsMap.containsKey(entry.getKey()))
                {
                    UIViewParameter paramComponent = (UIViewParameter) 
                            facesContext.getApplication().createComponent(
                                facesContext, UIViewParameter.COMPONENT_TYPE, null);
                    paramComponent.setId(root.createUniqueId(facesContext, null));
                    
                    paramComponent.setName(entry.getKey());
                    
                    ViewParam vp = entry.getValue();
                    
                    if (vp.converter() != null && vp.converter().length() > 0)
                    {
                        paramComponent.setConverter(facesContext.getApplication().createConverter(vp.converter()));
                    }

                    metadataFacet.getChildren().add(paramComponent);
                    viewParamsMap.put(paramComponent.getName(), paramComponent);
                }
            }
        }
    }
    
    public Object invokeAction(FacesContext facesContext, Object[] actionParams, MethodExpression method, 
            Class beanClass, String methodName, Class[] parameterTypes)
    {
        ActionControllerMetadata metadata = getActionControllers().get(beanClass);
        AnnotatedMethod target = null;
        for (AnnotatedMethod am : metadata.getViewActions())
        {
            if (methodName.equals(am.getJavaMember().getName()))
            {
                int parameterCount = (parameterTypes == null) ? 0 : parameterTypes.length;
                List<AnnotatedParameter> apList = am.getParameters();

                if (apList.size() == parameterCount)
                {
                    boolean valid = true;
                    for (int j = 0; j < apList.size(); j++)
                    {
                        Class paramClass = (Class) apList.get(j).getBaseType();
                        if (parameterTypes[j] != paramClass)
                        {
                            valid = false;
                            break;
                        }
                    }
                    if (valid)
                    {
                        target = am;
                        break;
                    }
                }
            }
        }
        if (target != null)
        {
            List<AnnotatedParameter> apList = target.getParameters();
            Object[] params = null;
            if (!apList.isEmpty())
            {
                params = new Object[apList.size()];
                for (int i = 0; i < apList.size(); i++)
                {
                    // Perform view param injection here!
                    AnnotatedParameter ap = apList.get(i);

                    if (ap.isAnnotationPresent(ViewParam.class))
                    {
                        //Special injection for ViewParam
                        ViewParam vanno = ap.getAnnotation(ViewParam.class);
                        Collection<UIViewParameter> parameters = ViewMetadata.getViewParameters(
                                facesContext.getViewRoot());
                        
                        for (UIViewParameter paramComponent : parameters)
                        {
                            if (vanno.value().equals(paramComponent.getName()))
                            {
                                params[i] = paramComponent.getValue();
                                break;
                            }
                        }
                    }
                    else
                    {
                        // Assume the parameter has qualifiers.
                        Set<Annotation> annotations = ap.getAnnotations();
                        Annotation[] annoArray = ap.getAnnotations().toArray(new Annotation[annotations.size()]);
                        Instance qualifiedInstance = objectBuilderInstances.select((Class)ap.getBaseType(), annoArray);
                        params[i] = qualifiedInstance.get();
                    }
                }
            }

            Object value = method.invoke(facesContext.getELContext(), params);
            
            if (value != null)
            {
                // Process the return value:
                if (value instanceof ActionResponse)
                {
                    try
                    {
                        ((ActionResponse)value).encodeAll(facesContext);
                    }
                    catch (IOException e)
                    {
                        throw new FacesException(e);
                    }
                    return null;
                }
                else
                {
                    return value;
                }
            }
        }
        else
        {
            throw new FacesException("Cannot locate target method to be annotated for: " + beanClass.getName() + 
                    " and method "+methodName);
        }
        return null;
    }
    
    public boolean viewExists(FacesContext facesContext, String viewId)
    {
        return Boolean.TRUE.equals(viewIds.get(viewId));
    }
    
    private static boolean isStringEmpty(String value)
    {
        if (value == null)
        {
            return true;
        }
        else if (value.length() <= 0)
        {
            return true;
        }
        return false;
    }
    
    public static class ActionControllerMetadata
    {
        private AnnotatedType type;

        private List<AnnotatedMethod> viewActions;
        
        public ActionControllerMetadata(AnnotatedType type)
        {
            this.type = type;
            this.viewActions = new ArrayList<AnnotatedMethod>();
            for (Iterator<AnnotatedMethod> it = type.getMethods().iterator(); it.hasNext(); )
            {
                AnnotatedMethod m = it.next();
                
                if (m.isAnnotationPresent(ViewAction.class))
                {
                    viewActions.add(m);
                }
            }
        }
        
        public List<AnnotatedMethod> getViewActions()
        {
            return viewActions;
        }

        public AnnotatedType getType()
        {
            return type;
        }
    }
    
    /**
     * NOTE: Taken from org.apache.catalina.deploy.SecurityConstraint
     * 
     * Does the specified request path match the specified URL pattern?
     * This method follows the same rules (in the same order) as those used
     * for mapping requests to servlets.
     *
     * @param path Context-relative request path to be checked
     *  (must start with '/')
     * @param pattern URL pattern to be compared against
     */
    public static boolean matchPattern(String path, String pattern)
    {
        // Normalize the argument strings
        if ((path == null) || (path.length() == 0))
        {
            path = "/";
        }
        if ((pattern == null) || (pattern.length() == 0))
        {
            pattern = "/";
        }

        // Check for exact match
        if (path.equals(pattern))
        {
            return (true);
        }

        // Check for path prefix matching
        if (pattern.startsWith("/") && pattern.endsWith("/*"))
        {
            pattern = pattern.substring(0, pattern.length() - 2);
            if (pattern.length() == 0)
            {
                return (true);  // "/*" is the same as "/"
            }
            if (path.endsWith("/"))
            {
                path = path.substring(0, path.length() - 1);
            }
            while (true)
            {
                if (pattern.equals(path))
                {
                    return (true);
                }
                int slash = path.lastIndexOf('/');
                if (slash <= 0)
                {
                    break;
                }
                path = path.substring(0, slash);
            }
            return (false);
        }

        // Check for suffix matching
        if (pattern.startsWith("*."))
        {
            int slash = path.lastIndexOf('/');
            int period = path.lastIndexOf('.');
            if ((slash >= 0) && (period > slash) &&
                path.endsWith(pattern.substring(1)))
            {
                return (true);
            }
            return (false);
        }

        // Check for universal mapping
        if (pattern.equals("/"))
        {
            return (true);
        }

        return (false);
    }
    
}
