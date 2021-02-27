/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.util;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/***********************************************************************************************************************
 *
 * A {@link ServletContextListener} that reads a configuration from a number of external sources and copy them to
 * the {@link ServletContext} as attributes.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class ExternalConfigurationServletContextListener implements ServletContextListener
  {
    private static final BootLogger log = new BootLogger(ExternalConfigurationServletContextListener.class);

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
        log.log("configurationPath: " + configurationPath);
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
        // do nothing
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    protected void loadProperties (final @Nonnull ServletContext servletContext,
                                   final @Nonnull String configurationFile)
      {
        final File file = new File(configurationFile);
        final Properties properties = new Properties();

        if (Boolean.getBoolean("nw.useSystemProperties"))
          {
            loadPropertiesFromSystemProperties(properties);
          }
        else if (file.exists())
          {
            try
              {
                loadProperties(properties, file);
              }
            catch (IOException e)
              {
                log.log(e);
              }
          }
        else
          {
            log.log(file.getAbsolutePath() + " does not exist");
          }

        putPropertiesIntoServletContext(servletContext, properties);
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
     * Returns the path of the directory containing the configuration file. It tries in order:
     *
     * 1. A path specified by the system property it.tidalwave.northernwind.configurationPath
     * 2. A jetty initialization parameter specified as:
     *
     * <Configure class="org.mortbay.jetty.webapp.WebAppContext">
     *       ....
     *       <Set name="initParams">
     *          <Map>
     *            <Entry>
     *             <Item>it.tidalwave.northernwind.configurationPath</Item>
     *             <Item>/some/path</Item>
     *            </Entry>
     *          </Map>
     *       </Set>
     * </Configure>
     *
     * 3. The .nw directory under the user home
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
            log.log("JNDI name not found: " + jndiName);
          }
        catch (Exception e)
          {
            log.log(e);
          }

        return System.getProperty("user.home") + "/.nw";
      }

    /*******************************************************************************************************************
     *
     * Loads properties from the system properties.
     *
     ******************************************************************************************************************/
    private static void loadPropertiesFromSystemProperties (final @Nonnull Properties properties)
      {
        log.log("using system properties");

        for (final Entry<Object, Object> entry : new TreeMap<>(System.getProperties()).entrySet())
          {
            final Object propertyName = entry.getKey();
            final Object propertyValue = entry.getValue();

            if (((String)propertyName).startsWith("nw."))
              {
                properties.put(propertyName, propertyValue);
              }
          }
      }

    /*******************************************************************************************************************
     *
     * Loads properties from a file.
     *
     ******************************************************************************************************************/
    private static void loadProperties (final @Nonnull Properties properties, final File file)
      throws IOException
      {
        log.log("reading properties from " + file);

        try (final InputStream is = new FileInputStream(file))
          {
            properties.load(is);
          }
      }

    /*******************************************************************************************************************
     *
     * Puts the properties into the ServletContext, as attributes.
     *
     ******************************************************************************************************************/
    private static void putPropertiesIntoServletContext (final @Nonnull ServletContext servletContext,
                                                         final @Nonnull Properties properties)
      {
        log.log("Copying properties to servlet context as attributes");
        final String nwcontextConfigLocation = computeConfigLocation(properties);
        properties.put("nw.contextConfigLocation", nwcontextConfigLocation);

        for (final Entry<Object, Object> entry : new TreeMap<>(properties).entrySet())
          {
            log.log(">>>> " + entry.getKey() + " = " + entry.getValue());
            servletContext.setAttribute(entry.getKey().toString(), entry.getValue().toString());
          }
      }

    /*******************************************************************************************************************
     *
     * Computes the Spring bean files path.
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String computeConfigLocation (final Properties properties)
      {
        final String nwBeans = properties.getProperty("nw.beans", "");
        final StringBuilder builder = new StringBuilder();
        String separator = "";

        for (final String nwBean : nwBeans.split(","))
          {
            if (!nwBean.trim().equals(""))
              {
                builder.append(separator).append(String.format("classpath:/META-INF/%sBeans.xml", nwBean.trim()));
                separator = ",";
              }
          }

        return "classpath*:/META-INF/*AutoBeans.xml,"
             + "classpath*:/META-INF/WebConfigurationBeans.xml," + builder.toString();
      }
  }
