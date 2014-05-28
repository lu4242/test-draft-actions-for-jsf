/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.myfaces.mvc.api.component;

import javax.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;

/**
 *
 * @author lu4242
 */
@JSFRenderer(renderKitId = "HTML_BASIC", 
        type = "org.apache.myfaces.mvc.DefineLinkComponent", 
        family="javax.faces.OutcomeTarget")
public class DefineLinkRenderer extends Renderer
{
    
}
