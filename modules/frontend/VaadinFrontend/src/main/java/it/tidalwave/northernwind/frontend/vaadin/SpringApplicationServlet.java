/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.vaadin;

import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/***********************************************************************************************************************
 *
 * See http://vaadin.com/wiki/-/wiki/Main/Creating%20JEE6%20Vaadin%20Applications (option #3)
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class SpringApplicationServlet extends AbstractApplicationServlet 
  {
    private WebApplicationContext applicationContext;
    
    private Class<? extends Application> applicationClass;
    
    private String applicationBean;

    @Override
    public void init (final @Nonnull ServletConfig servletConfig)
      throws ServletException
      {
        super.init(servletConfig);
        applicationBean = servletConfig.getInitParameter("applicationBean");
      
        if (applicationBean == null) 
          {
            throw new ServletException("ApplicationBean not specified in servlet parameters");
          }
      
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletConfig.getServletContext());
        applicationClass = (Class<? extends Application>) applicationContext.getType(applicationBean);
      }

    @Override @Nonnull
    protected Class<? extends Application> getApplicationClass()
      throws ClassNotFoundException 
      {
        return applicationClass;
      }

    @Override @Nonnull
    protected Application getNewApplication (final @Nonnull HttpServletRequest request)
      {
        return (Application)applicationContext.getBean(applicationBean);
      }
  } 