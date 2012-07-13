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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.util;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.Cleanup;
import static it.tidalwave.northernwind.frontend.util.BootLogger.*;
import javax.naming.NameNotFoundException;

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
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        final ServletContext servletContext = event.getServletContext();
        final String configurationPath = getConfigurationPath(servletContext) + "/configuration.properties";
        log(">>>> configurationPath: " + configurationPath);
        loadProperties(servletContext, configurationPath);
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
        
        if (Boolean.getBoolean("nw.useSystemProperties"))
          {
            log("Using system properties, ignoring any configuration file");
            
            final Properties properties = new Properties();
            
            for (final Entry<Object, Object> entry : new TreeMap<Object, Object>(System.getProperties()).entrySet())
              {
                final Object propertyName = entry.getKey();
                final Object propertyValue = entry.getValue();
                
                if (((String)propertyName).startsWith("nw."))
                  {
                    properties.put(propertyName, propertyValue);
                  }

                loadProperties(servletContext, properties);
              }
          }
        else if (file.exists())
          {
            try
              {                
                final Properties properties = loadProperties(configurationFile);
                loadProperties(servletContext, properties);
              }
            catch (IOException e)
              {
                log(e);
              }
          }
        else
          {
            log(file.getAbsolutePath() + " does not exist");
          }
      }
    
    protected void loadProperties (final @Nonnull ServletContext servletContext, final @Nonnull Properties properties)
      {
        copyPropertiesToServletContextAttributes(properties, servletContext);                
        servletContext.setAttribute("nwcontextConfigLocation", computeConfigLocation(properties));
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
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getConfigurationPath (final @Nonnull ServletContext servletContext)
      {
        String configurationPath = servletContext.getInitParameter("it.tidalwave.northernwind.configurationPath");
        
        if (configurationPath != null)
          {
            return configurationPath;   
          }
        
        final String jndiName = "org.mortbay.jetty.plus.naming.EnvEntry/it.tidalwave.northernwind.configurationPath";
        
        try // Jetty specific JNDI setting - see e.g. http://stackoverflow.com/questions/3895047/jetty-set-system-property 
          {
            final InitialContext context = new InitialContext();
            final Object env = context.lookup(jndiName);
            final Class<?> envClass = env.getClass();
            final Method method = envClass.getDeclaredMethod("getObjectToBind");
            return (String)method.invoke(env);
          }
        catch (NameNotFoundException e)
          {
            BootLogger.log("JNDI name not found: " + jndiName);  
          }
        catch (Exception e) 
          {
            BootLogger.log(e);
//            e.printStackTrace(); // FIXME  
          }
          
        return System.getProperty("user.home") + "/.nw";
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
        
        final String contextConfigLocation = "classpath*:/META-INF/*AutoBeans.xml,classpath*:/META-INF/WebConfigurationBeans.xml," + builder.toString();
        log(">>>> contextConfigLocation: " + contextConfigLocation);
        
        return contextConfigLocation;
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
