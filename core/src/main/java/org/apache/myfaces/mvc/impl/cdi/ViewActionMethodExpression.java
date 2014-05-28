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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.view.facelets.util.ReflectionUtil;

/**
 *
 * @author lu4242
 */
public class ViewActionMethodExpression extends MethodExpression implements Externalizable
{
    private Class clazz; 
    
    private MethodExpression method;
    
    private String methodName;
    
    private Class[] parameterTypes;

    public ViewActionMethodExpression()
    {
    }

    public ViewActionMethodExpression(Class clazz, MethodExpression method, String methodName, Class[] parameterTypes)
    {
        this.clazz = clazz;
        this.method = method;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }
    
    @Override
    public MethodInfo getMethodInfo(ELContext context) 
            throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException
    {
        return null;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) 
            throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException
    {
        FacesContext facesContext = getFacesContext(context);
        
        // The idea is this method goes into the action of the viewAction component, but do some 
        // special pre/post processing:
        //
        // 1. This method expression receives as parameter an ActionEvent. We need to fill the parameters
        // so we can target properly the method in the bean.
        // 2. The return value for the wrapped method expression is different from the one in the action
        // and could have a different meaning.
        
        ActionControllerHolderImpl actionControllerHolder = ActionControllerHolderImpl.getActionControllerHolder(facesContext);
        
        Object value = actionControllerHolder.invokeAction(facesContext, params, method, clazz, methodName, parameterTypes);
        
        return value;
    }
    
    private FacesContext getFacesContext(ELContext context)
    {
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        if (facesContext == null)
        {
            facesContext = FacesContext.getCurrentInstance();
        }
        return facesContext;
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj;
    }

    @Override
    public int hashCode()
    {
        return this.hashCode();
    }

    @Override
    public String getExpressionString()
    {
        return null;
    }

    @Override
    public boolean isLiteralText()
    {
        return false;
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(clazz.getName());
        out.writeObject(method);
        out.writeUTF(methodName);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        clazz = ReflectionUtil.forName(in.readUTF());
        method = (MethodExpression) in.readObject();
        methodName = in.readUTF();
    }
    
}
