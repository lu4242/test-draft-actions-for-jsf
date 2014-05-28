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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import org.apache.myfaces.cdi.util.CDIUtils;
import org.apache.myfaces.mvc.api.annotation.ActionController;

/**
 *
 * @author lu4242
 */
public class MvcCdiExtension implements Extension
{
   private static final Logger log = Logger.getLogger(MvcCdiExtension.class.getName());
   
   private List<AnnotatedType> actionControllers = new ArrayList<AnnotatedType>();
    
   void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager bm)
   {
       // Register the qualifier ActionController, to found later the related beans.
       event.addQualifier(ActionController.class);
       actionControllers.clear();
       
        AnnotatedType bean = bm.createAnnotatedType(ActionControllerHolderImpl.class);
        event.addAnnotatedType(bean);       
   }

   <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat, BeanManager bm)
   {
      //log.info("scanning type: " + pat.getAnnotatedType().getJavaClass().getName());

      // Step 1: define if the annotated type correspond to a bean.
      // Step 2: if it is a bean, we should derive the way how it is instantiated by CDI.
      // Step 3: add it to the controller structure, so the controller can find the bean and
      //         call the method.

      if (pat.getAnnotatedType().isAnnotationPresent(ActionController.class))
      {
          //log.info("Bean annotated with ActionController");
          actionControllers.add(pat.getAnnotatedType());
      }
   }
   
   void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm)
   {
       //log.info("finished the scanning process");
   }

   void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager bm)
   {
       ActionControllerHolderImpl holder = (ActionControllerHolderImpl) CDIUtils.lookup(bm, "oam_mvc_ACTION_CONTROLLER");
       for (AnnotatedType type : actionControllers)
       {
           holder.addActionController(type);
       }
       holder.init();
   }
}
