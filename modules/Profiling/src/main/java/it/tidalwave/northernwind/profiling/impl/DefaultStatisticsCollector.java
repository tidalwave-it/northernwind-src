/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.profiling.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
    private final AggregateSummaryStatistics globalStatistics = new AggregateSummaryStatistics();
    
    private final AggregateSummaryStatistics recentStatistics = new AggregateSummaryStatistics();
    
    private final SummaryStatistics globalRequestStatistics = globalStatistics.createContributingStatistics();
    
    private final SummaryStatistics recentRequestStatistics = recentStatistics.createContributingStatistics();
    
    @Override
    public void registerRequest (final @Nonnull Request request, final @Nonnegative long elapsedTime) 
      {
        log.info(">>>> {} completed in {} msec", request, elapsedTime);
        
        synchronized (this) 
          {
            globalRequestStatistics.addValue(elapsedTime);
            recentRequestStatistics.addValue(elapsedTime);
          }
      }
    
    public void dumpStatistics()
      {
        synchronized (this) 
          {
            dump("OVERALL", globalRequestStatistics);
            dump("RECENT ", recentRequestStatistics);
            recentRequestStatistics.clear();
          }
      }
    
    private void dump (final @Nonnull String name, final @Nonnull SummaryStatistics statistics)
      {
        log.info("STATS: {} request count: {} completion time min: {} avg: {} max: {} dev: {}", new Object[]
                {
                  name,
                  statistics.getN(),
                  statistics.getMin(),
                  statistics.getMean(),
                  statistics.getMax(),
                  statistics.getStandardDeviation()
                });
      }
  }