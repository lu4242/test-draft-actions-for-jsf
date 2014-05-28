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

import javax.faces.component.UIOutcomeTarget;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;

/**
 *
 */
@JSFComponent(name = "ma:defineLink",
              clazz =  "org.apache.myfaces.mvc.api.component.DefineLinkComponent",
              defaultRendererType = "org.apache.myfaces.mvc.DefineLinkComponent")
public class AbstractDefineLinkComponent extends UIOutcomeTarget
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.mvc.DefineLinkComponent";

    public AbstractDefineLinkComponent()
    {
        super();
    }
    
    @Override
    public String getOutcome()
    {
        String outcome = super.getOutcome();
        if (outcome != null && outcome.length() > 0)
        {
            return outcome;
        }
        outcome = getFacesContext().getViewRoot().getViewId();
        return outcome;
    }
}
