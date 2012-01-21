/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.util;

import javax.servlet.ServletContext;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/***********************************************************************************************************************
 *
 * A specialization of {@link ContextLoaderListener} that uses the {@code ServletContext} attributes rather than
 * init parameters.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ContextAttributeContextLoaderListener extends ContextLoaderListener
  {
    @Override
    protected WebApplicationContext createWebApplicationContext(ServletContext sc, ApplicationContext parent)
      {
        Class<?> contextClass = determineContextClass(sc);
        
        if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) 
          {
            throw new ApplicationContextException("Custom context class [" + contextClass.getName() +
                                "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
          }
          
    ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

    // Assign the best possible id value.
    if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) 
      {
        // Servlet <= 2.4: resort to name specified in web.xml, if any.
        String servletContextName = sc.getServletContextName();
        wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX
                + ObjectUtils.getDisplayString(servletContextName));
      }
    else 
      {
        // Servlet 2.5's getContextPath available!
        try 
          {
            String contextPath = (String)ServletContext.class.getMethod("getContextPath").invoke(sc);
            wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX
                    + ObjectUtils.getDisplayString(contextPath));
          }
        catch (Exception ex) 
          {
            throw new IllegalStateException("Failed to invoke Servlet 2.5 getContextPath method", ex);
          }
      }

    wac.setParent(parent);
    wac.setServletContext(sc);
    wac.setConfigLocation((String)sc.getAttribute("nwcontextConfigLocation"));
//		wac.setConfigLocation(sc.getInitParameter(CONFIG_LOCATION_PARAM));
    customizeContext(sc, wac);
    wac.refresh();
    return wac;
  }
}
