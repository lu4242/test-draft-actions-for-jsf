/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.myfaces.mvc.example;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.myfaces.mvc.api.annotation.ActionController;

/**
 *
 * @author lu4242
 */
@Named("jsonBean")
@ActionController
@RequestScoped
public class JsonBean
{
    
    private String value;
    
    /**
     * This is referenced through a component called ma:defineAction
     * @return 
     */
    public String renderJSONForAutoComplete()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseContentType("application/json");
        externalContext.setResponseCharacterEncoding("UTF-8");
        try
        {
            externalContext.getResponseOutputWriter().write("[\""+getValue()+" Option A\", \"Option B\"]");
        } catch (IOException ex)
        {
            Logger.getLogger(JsonBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
}
