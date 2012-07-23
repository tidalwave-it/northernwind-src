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
package it.tidalwave.northernwind.profiling.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.impl.model.aspect.StatisticsCollector;
import it.tidalwave.northernwind.core.model.Request;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultStatisticsCollector implements StatisticsCollector
  {
    private final Stats requestStats = new Stats("REQUEST");
    
    private final ThreadLocal<Long> baseTime = new ThreadLocal<>();
    
    @Override
    public void onRequestBegin (final @Nonnull Request request) 
      {
        baseTime.set(System.currentTimeMillis());
      }
    
    @Override
    public void onRequestEnd (final @Nonnull Request request) 
      {
        final long elapsedTime = System.currentTimeMillis() - baseTime.get();
        baseTime.remove();
        log.info(">>>> {} completed in {} msec", request, elapsedTime);
        
        synchronized (this) 
          {
            requestStats.addValue(elapsedTime);
          }
      }
    
    public void dumpStatistics()
      {
        synchronized (this) 
          {
            log.info("STATS {}", requestStats.globalAsString());
            log.info("STATS {}", requestStats.recentAsString());
            requestStats.clearRecent();
          }
      }
  }
