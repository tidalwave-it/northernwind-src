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

import java.util.Collections;
import javax.annotation.Nonnull;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Dumps system properties at startup.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class SystemConfigurationLoggerServletContextListener implements ServletContextListener
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        log.info("**************** System properties:");

        for (final Entry<Object, Object> entry : new TreeMap<>(System.getProperties()).entrySet())
          {
            log.info("{} = {}", entry.getKey(), entry.getValue());
          }

        try
          {
            final InitialContext context = new InitialContext();
            log.info("**************** JNDI Environment:");

            for (final Entry<?, ?> entry : new TreeMap<>(context.getEnvironment()).entrySet())
              {
                log.info("{} = {}", entry.getKey(), entry.getValue());
              }

            log.info("**************** JNDI Bindings:");

            for (final Binding binding : Collections.list(context.listBindings("")))
              {
                log.info("{} = {}", binding.getNameInNamespace(), binding.getObject());
              }
          }
        catch (NamingException e)
          {
            log.warn("No JNDI: {}", e.toString());
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
      }
  }
