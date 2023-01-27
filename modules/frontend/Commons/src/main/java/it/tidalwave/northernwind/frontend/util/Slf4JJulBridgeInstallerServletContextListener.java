/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.logging.LogManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.bridge.SLF4JBridgeHandler;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Installs the Slf4J bridge for JUL. Needed only if a library using JUL is deployed into a webapp.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class Slf4JJulBridgeInstallerServletContextListener implements ServletContextListener
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (@Nonnull final ServletContextEvent event)
      {
        final var rootLogger = LogManager.getLogManager().getLogger("");
        final var handlers = rootLogger.getHandlers();

        for (final var handler : handlers)
          {
            rootLogger.removeHandler(handler);
          }

        SLF4JBridgeHandler.install();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextDestroyed (@Nonnull final ServletContextEvent event)
      {
        // do nothing
      }
  }
