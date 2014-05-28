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
import javax.faces.context.FacesContext;

/**
 *
 */
public class TextResponse extends ActionResponse
{
    
    public final String value;
    
    public final String characterEncoding;

    public final String contentType;

    public TextResponse(String value)
    {
        this(value, null, null);
    }
    
    public TextResponse(String value, String characterEncoding)
    {
        this(value, characterEncoding, null);
    }
    
    public TextResponse(String value, String characterEncoding, String contentType)
    {
        this.value = value;
        this.characterEncoding = characterEncoding == null ? "UTF-8" : characterEncoding;
        this.contentType = contentType == null ? "text/plain" : contentType;
    }

    public String getValue()
    {
        return value;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getCharacterEncoding()
    {
        return characterEncoding;
    }

    @Override
    public void encodeBegin(FacesContext facesContext) throws IOException
    {
        facesContext.getExternalContext().setResponseContentType(getContentType());
        facesContext.getExternalContext().setResponseCharacterEncoding(getCharacterEncoding());
    }

    @Override
    public void encodeContent(FacesContext facesContext) throws IOException
    {
        facesContext.getExternalContext().getResponseOutputWriter().write(value);
    }

    @Override
    public void encodeEnd(FacesContext facesContext) throws IOException
    {
        facesContext.responseComplete();
    }
}
