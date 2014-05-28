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
package org.apache.myfaces.mvc.example;

import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.myfaces.mvc.api.annotation.ActionController;
import org.apache.myfaces.mvc.api.annotation.ViewAction;
import org.apache.myfaces.mvc.api.annotation.ViewParam;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Managed bean for the sayHello page example
 */
@Named("exporter")
@ActionController
@RequestScoped
public class ExcelExportBean
{

    public ExcelExportBean()
    {
    }
    
    @ViewAction(value="/exporter.xhtml", 
                params=@ViewParam(value="action", expectedValue="exportExcel")
                )
    public void exportExcel(@ViewParam("id") int id, @InjectFacesContext FacesContext facesContext) throws IOException
    {
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseContentType("application/vnd.ms-excel");
        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"my_"+id+".xls\"");
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        int i = 0;
        HSSFRow row = sheet.createRow((short) i);
        int j = 0;
        row.createCell(j++).setCellValue(id);
        
        workbook.write(externalContext.getResponseOutputStream());
        facesContext.responseComplete();
    }
}
