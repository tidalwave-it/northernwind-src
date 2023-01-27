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
package it.tidalwave.northernwind.profiling.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.profiling.StatisticsCollector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultStatisticsCollector implements StatisticsCollector
  {
    class Sample
      {
        private final long elapsedBaseTime = System.nanoTime();

        private final long cpuBaseTime = threadMxBean.getCurrentThreadCpuTime();

        private final long userBaseTime = threadMxBean.getCurrentThreadUserTime();

        @Getter
        private long elapsedTime;

        @Getter
        private long cpuTime;

        @Getter
        private long userTime;

        public void stop()
          {
            elapsedTime = System.nanoTime() - elapsedBaseTime;
            cpuTime = threadMxBean.getCurrentThreadCpuTime() - cpuBaseTime;
            userTime = threadMxBean.getCurrentThreadUserTime() - userBaseTime;
          }
      }

    private final Stats elapsedTimeStats = new Stats("REQUEST ELAPSED TIME", 1E-6);

    private final Stats cpuTimeStats = new Stats("REQUEST CPU TIME    ", 1E-6);

    private final Stats userTimeStats = new Stats("REQUEST USER TIME   ", 1E-6);

    private final ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean( );

    private final ThreadLocal<Sample> sampleHolder = new ThreadLocal<>();

    @Override
    public void onRequestBegin (@Nonnull final Request request)
      {
        sampleHolder.set(new Sample());
      }

    @Override
    public void onRequestEnd (@Nonnull final Request request)
      {
        final var sample = sampleHolder.get();
        sample.stop();
        sampleHolder.remove();
        log.info(">>>> {} completed in {} msec", request, sample.getElapsedTime() * 1E-6);

        synchronized (this)
          {
            elapsedTimeStats.addValue(sample.getElapsedTime());
            cpuTimeStats.addValue(sample.getCpuTime());
            userTimeStats.addValue(sample.getUserTime());
          }
      }

    @SuppressWarnings("squid:S1192")
    public void dumpStatistics()
      {
        synchronized (this)
          {
            log.info("STATS {}", elapsedTimeStats.globalAsString());
            log.info("STATS {}", elapsedTimeStats.recentAsString());
            log.info("STATS {}", cpuTimeStats.globalAsString());
            log.info("STATS {}", cpuTimeStats.recentAsString());
            log.info("STATS {}", userTimeStats.globalAsString());
            log.info("STATS {}", userTimeStats.recentAsString());
            elapsedTimeStats.clearRecent();
            cpuTimeStats.clearRecent();
            userTimeStats.clearRecent();
          }
      }
  }
