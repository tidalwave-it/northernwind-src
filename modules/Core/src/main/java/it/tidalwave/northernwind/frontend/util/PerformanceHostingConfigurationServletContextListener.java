/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.util;

import javax.annotation.Nonnull;
import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/***********************************************************************************************************************
 *
 * A {@link ServletContextListener} for integrating with Tomcat hosting at PerformanceHosting.net
 * 
 * @author  Fabrizio Giudici
 * @version $Id: Slf4JJulBrigdeInstallerServletContextListener.java,v 64e867bc71e7 2011/07/27 13:49:56 fabrizio $
 *
 **********************************************************************************************************************/
public class PerformanceHostingConfigurationServletContextListener extends ExternalConfigurationServletContextListener
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        final ServletContext servletContext = event.getServletContext();
        final String contextPath = servletContext.getContextPath();
        final String realPath = servletContext.getRealPath(".probe");
        log(">>>> contextPath: " + contextPath);
        log(">>>> realPath:    " + realPath);
        
        final String x = realPath.replace(File.pathSeparator + "webapps" + contextPath + File.pathSeparator + ".probe", "");
        
        if (x.startsWith("/home/fgiudici")) // FIXME: check instead the hostname, put all this method within the if
          {
            final String[] x2 = x.split("/");
            final String home = "/" + x2[1] + "/" + x2[2];
            final String domain = x2[6] + "." + x2[4];
            final String configurationFile = home + "/.nw/" + domain + contextPath + "/configuration.properties";
            final String logBackConfigurationFile = home + "/.nw/" + domain + contextPath + "/logback.xml";
           
            // FIXME: this will collide with other webapps in the same Tomcat - try to set it into JNDI and then use
            // file inclusion from the embedded logback.xml
            System.setProperty("logback.configurationFile", logBackConfigurationFile);
            
            log(">>>> home:   " + home);
            log(">>>> domain: " + domain);
            
            loadProperties(servletContext, configurationFile);
            // TODO: use the configuration file for Spring
          }
      }
  }
