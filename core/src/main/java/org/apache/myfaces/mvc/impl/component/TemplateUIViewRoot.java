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
package org.apache.myfaces.mvc.impl.component;

import java.util.Collections;
import java.util.Map;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 *
 * @author lu4242
 */
@JSFComponent(type = "oam.mvc.UIViewRoot")
public class TemplateUIViewRoot extends UIViewRoot
{
    private UIViewRoot parentViewRoot;

    public TemplateUIViewRoot()
    {
        super();
    }
    
    public void setParentViewRoot(UIViewRoot root)
    {
        this.parentViewRoot = root;
    }

    @Override
    public Map<String, Object> getViewMap()
    {
        return getViewMap(true);
    }

    @Override
    public Map<String, Object> getViewMap(boolean create)
    {
        if (parentViewRoot != null)
        {
            return parentViewRoot.getViewMap(create);
        }
        return Collections.emptyMap();
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
    }

    @Override
    public Object saveState(FacesContext facesContext)
    {
        return null;
    }
}
