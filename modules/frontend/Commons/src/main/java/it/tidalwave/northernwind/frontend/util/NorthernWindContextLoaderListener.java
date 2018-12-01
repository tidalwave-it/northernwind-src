/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.web.context.ContextLoaderListener;
import lombok.experimental.Delegate;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * A decorator for the Spring {@code ContextLoaderListener} that takes care of Spring configuration and handles
 * initialization errors.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class NorthernWindContextLoaderListener extends ContextLoaderListener
  {
    private static final BootLogger log = new BootLogger(NorthernWindContextLoaderListener.class);

    // Spring stuff
    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    public static final String ATTRIBUTE_BOOT_THROWABLE = "it.tidalwave.northernwind.bootThrowable";

    private ServletContextListener delegate;

    /*******************************************************************************************************************
     *
     * A decorator of {@link ServletContext} that:
     *
     * <ul>
     * <li>retrieves the 'contextConfigLocation' init parameter from the 'nw.contextConfigLocation' attribute;</li>
     * <li>retrieves all the init parameters whose name starts with 'nw.*' from attributes with the same name.</li>
     * </ul>
     *
     * This would be probably unneeded if we would able to have PropertySourcesPlaceholderConfigurer read also
     * context attributes.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    static class ServletContextDecorator implements ServletContext
      {
        interface Exclusions
          {
            public String getInitParameter (String string);
          }

        @Nonnull @Delegate(excludes = Exclusions.class)
        private final ServletContext delegate;

        @Override
        public String getInitParameter (final @Nonnull String name)
          {
            final String value = name.equals(CONTEXT_CONFIG_LOCATION)
                    ? (String)getAttribute("nw.contextConfigLocation")
                    : name.startsWith("nw.") ? (String)getAttribute(name)
                                             : delegate.getInitParameter(name);
            delegate.log("ServletContext parameter " + name + " = " + value);
    //        Thread.dumpStack();
            return value;
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        try
          {
            log.log("Initializing Spring...");
            super.contextInitialized(new ServletContextEvent(new ServletContextDecorator(event.getServletContext())));
          }
        catch (Throwable t)
          {
            event.getServletContext().setAttribute(ATTRIBUTE_BOOT_THROWABLE, t);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextDestroyed (final @Nonnull ServletContextEvent event)
      {
        delegate.contextDestroyed(event);
      }
  }
