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
package it.tidalwave.northernwind.frontend.util;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import static it.tidalwave.northernwind.frontend.util.BootLogger.*;

/***********************************************************************************************************************
 *
 * A {@link ServletContextListener} for integrating with Tomcat hosting at PerformanceHosting.net
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
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
        if (getLocalHostName().endsWith(".performancehosting.net"))
          {
            final ServletContext servletContext = event.getServletContext();
            final String contextPath = servletContext.getContextPath();
            final String realPath = servletContext.getRealPath("");
            log(">>>> contextPath: " + contextPath);
            log(">>>> realPath:    " + realPath);
            
            final String x = realPath.replace(File.pathSeparator + "webapps" + contextPath + File.pathSeparator, "");

            final String[] x2 = x.split("/");
            final String home = "/" + x2[1] + "/" + x2[2];
            final String domain = x2[6] + "." + x2[4];
            final String configurationFile = home + "/.nw/" + domain + contextPath + "/configuration.properties";
            final String logBackConfigurationFile = home + "/.nw/" + domain + contextPath + "/logback.xml";

            // FIXME: this will collide with other webapps in the same Tomcat - try to set it into JNDI and then use
            // file inclusion from the embedded logback.xml
            System.setProperty("logback.configurationFile", logBackConfigurationFile);
//                enableLogging();

            log(">>>> home:   " + home);
            log(">>>> domain: " + domain);

            loadProperties(servletContext, configurationFile);
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getLocalHostName()
      {
        try 
          {
            final String hostName = InetAddress.getLocalHost().getCanonicalHostName();
            log(">>>> host name: " + hostName);
            return hostName;
          }
        catch (UnknownHostException e) 
          {
            return "?";
          }
      }
  }
