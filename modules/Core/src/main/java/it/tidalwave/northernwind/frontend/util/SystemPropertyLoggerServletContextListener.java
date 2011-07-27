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
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * Dumps system properties at startup.
 * 
 * @author  Fabrizio Giudici
 * @version $Id: Slf4JJulBrigdeInstallerServletContextListener.java,v 64e867bc71e7 2011/07/27 13:49:56 fabrizio $
 *
 **********************************************************************************************************************/
@Slf4j
public class SystemPropertyLoggerServletContextListener implements ServletContextListener
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void contextInitialized (final @Nonnull ServletContextEvent event)
      {
        log.info("System properties:");
        
        for (final Entry<Object, Object> entry : new TreeMap<Object, Object>(System.getProperties()).entrySet())
          {
            log.info(">>>> {} = {}", entry.getKey(), entry.getValue());
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
