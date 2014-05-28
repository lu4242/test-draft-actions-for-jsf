/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.myfaces.mvc.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.myfaces.mvc.api.annotation.ActionController;
import org.apache.myfaces.mvc.api.annotation.ViewAction;

/**
 *
 */
@Named("globalActionBean")
@ActionController
@ApplicationScoped
public class GlobalActionBean
{
        
    /** 
     * This is an action that is defined "globally", so for every view processed by JSF, an f:viewAction
     * component will be added that execute this method.
     */
    @ViewAction("/*")
    public void method2()
    {
        System.out.println("Global View Action Processed!");
    }
}
