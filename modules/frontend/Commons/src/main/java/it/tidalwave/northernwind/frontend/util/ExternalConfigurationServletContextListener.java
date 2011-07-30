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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***********************************************************************************************************************
 *
 * A {@link ServletContextListener} that reads a configuration from $HOME/.nw/configuration.properties
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ExternalConfigurationServletContextListener implements ServletContextListener
  {
//    @CheckForNull
//    private static Logger log;
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        final String configurationFile = System.getProperty("user.home") + "/.nw/configuration.properties";
        loadProperties(event.getServletContext(), configurationFile);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextDestroyed (final @Nonnull ServletContextEvent event) 
      {
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected void loadProperties (final @Nonnull ServletContext servletContext, final @Nonnull String configurationFile)
      {  
        final File file = new File(configurationFile);
        
        if (!file.exists())
          {
            log(file.getAbsolutePath() + " does not exist");
          }
        else
          {
            try
              {                
                final Properties properties = loadProperties(configurationFile);
                copyPropertiesToServletContextAttributes(properties, servletContext);                
                servletContext.setAttribute("nwcontextConfigLocation", computeConfigLocation(properties));
              }
            catch (IOException e)
              {
                log(e.toString());
                e.printStackTrace(); // FIXME  
              }
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected static void enableLogging()
      {
        // Logging should have been configured at this point
//        log = LoggerFactory.getLogger(ExternalConfigurationServletContextListener.class);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Properties loadProperties(final String configurationFile) throws FileNotFoundException, IOException 
      {
        log(">>>> reading properties from " + configurationFile);
        final Properties properties = new Properties();
        final @Cleanup InputStream is = new FileInputStream(configurationFile);
        properties.load(is);
        is.close();
        return properties;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeConfigLocation (final Properties properties) 
      {
        final StringBuilder builder = new StringBuilder();
        String separator = "";
      
        for (final String nwBeans : properties.getProperty("nw.beans", "").split(","))
          {
            if (!nwBeans.trim().equals(""))
              {
                builder.append(separator).append(String.format("classpath:/META-INF/%sBeans.xml", nwBeans.trim()));
                separator = ",";
              }
          }
        
        final String contextConfigLocation = "classpath*:/META-INF/*AutoBeans.xml," + builder.toString();
        log(">>>> contextConfigLocation: " + contextConfigLocation);
        
        return contextConfigLocation;
      }
    
    /*******************************************************************************************************************
     *
     * We can't log to the real thing, since we first need to compute the path of the logging file. Logging to the real
     * thing would instantiate the logging facility before we have a chance to configure it.
     *
     ******************************************************************************************************************/
    protected static void log (final @Nonnull String string)
      {
    //        if (log != null)
    //          {
    //            log.info(string);  
    //          }
    //        else
          {
            System.err.println(string);
          } 
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void copyPropertiesToServletContextAttributes (final @Nonnull Properties properties,
                                                           final @Nonnull ServletContext servletContext)
      {
        for (final Entry<Object, Object> entry : new TreeMap<Object, Object>(properties).entrySet())
          {
            log(">>>> " + entry.getKey() + " = " + entry.getValue());
            servletContext.setAttribute(entry.getKey().toString(), entry.getValue().toString());                       
          }
      }
  }
