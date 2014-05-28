/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.myfaces.mvc.example;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.myfaces.mvc.api.annotation.ActionController;
import org.apache.myfaces.mvc.api.annotation.ViewAction;
import org.apache.myfaces.mvc.api.annotation.ViewParam;
import org.apache.myfaces.mvc.api.model.response.ActionResponse;
import org.apache.myfaces.mvc.api.model.response.JSFTemplateResponse;
import org.apache.myfaces.mvc.api.model.response.TextResponse;

/**
 *
 * 
 */
@Named("simpleViewActionBean")
@ActionController
@RequestScoped
public class SimpleViewActionBean
{
    
    @ViewAction(value="/testSimpleViewAction.xhtml", 
                params=@ViewParam(value="action", expectedValue="exportText")
                )
    public ActionResponse exportText(@ViewParam("id") int id, @InjectFacesContext FacesContext facesContext)
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        //externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"my.txt\"");
        
        return new TextResponse("Hello World "+id+"!");
    }

    @ViewAction(value="/testSimpleViewAction.xhtml", 
                params=@ViewParam(value="action", expectedValue="renderMessage")
                )
    public ActionResponse renderJSFTemplate()
    {
        return new JSFTemplateResponse("/META-INF/templates/showMessage.xhtml");
    }    
}
