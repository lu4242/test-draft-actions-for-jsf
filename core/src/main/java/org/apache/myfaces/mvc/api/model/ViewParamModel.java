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
package org.apache.myfaces.mvc.api.model;

import java.io.Serializable;

/**
 *
 */
public class ViewParamModel implements Serializable
{
    private String name;
    private String value;
    private boolean checkEquals;

    public ViewParamModel()
    {
        this(null,null,false);
    }

    public ViewParamModel(String name, String value, boolean checkEquals)
    {
        this.name = name;
        this.value = value;
        this.checkEquals = checkEquals;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the checkEquals
     */
    public boolean isCheckEquals()
    {
        return checkEquals;
    }

    /**
     * @param checkEquals the checkEquals to set
     */
    public void setCheckEquals(boolean checkEquals)
    {
        this.checkEquals = checkEquals;
    }

    
}
