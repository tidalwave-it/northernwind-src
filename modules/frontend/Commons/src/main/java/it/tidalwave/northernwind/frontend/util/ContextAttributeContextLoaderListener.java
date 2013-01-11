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
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.springframework.web.context.ContextLoaderListener;
import lombok.Delegate;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * A specialization of {@link ContextLoaderListener} that uses the {@code ServletContext} attributes rather than
 * init parameters.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
class DynamicConfigLocationServletContext implements ServletContext
  {
    public static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";

    interface Exclusions
      {
        public String getInitParameter (String string);
      }

    @Nonnull @Delegate(excludes = Exclusions.class)
    private final ServletContext delegate;

    @Nonnull
    private final String contextConfigLocation;

    @Override
    public String getInitParameter (final @Nonnull String name)
      {
        final String value = name.equals(CONTEXT_CONFIG_LOCATION) ? contextConfigLocation
                                                                  : delegate.getInitParameter(name);
        delegate.log("ServletContext parameter " + name + " = " + value);
        return value;
      }
  }

public class ContextAttributeContextLoaderListener extends ContextLoaderListener
  {
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        final ServletContext servletContext = event.getServletContext();
        final String contextConfigLocation = (String)servletContext.getAttribute("nwcontextConfigLocation");
        final ServletContext servletContextDelegate =
                new DynamicConfigLocationServletContext(servletContext, contextConfigLocation);
        servletContext.log("contextConfigLocation: " + contextConfigLocation);

        super.contextInitialized(new ServletContextEvent(servletContextDelegate));
      }
  }
