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
package it.tidalwave.northernwind.profiling.impl;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class Stats
  {
    private static final String PATTERN = "%s %s | count: %6d | min: %7.2f | avg: %7.2f | max: %9.2f | dev: %7.2f";

    @Nonnull
    private final String name;

    @Nonnegative
    private final double scale;

    private final SummaryStatistics globalStats = new SummaryStatistics();

    private final SummaryStatistics recentStats = new SummaryStatistics();

    public synchronized void addValue (final long elapsedTime)
      {
        globalStats.addValue(elapsedTime);
        recentStats.addValue(elapsedTime);
      }

    public synchronized void clearRecent()
      {
        recentStats.clear();
      }

    @Nonnull
    public synchronized String globalAsString()
      {
        return String.format(PATTERN,
                             "TOTAL ",
                             name,
                             globalStats.getN(),
                             globalStats.getMin() * scale,
                             globalStats.getMean() * scale,
                             globalStats.getMax() * scale,
                             globalStats.getStandardDeviation() * scale);
      }

    @Nonnull
    public synchronized String recentAsString()
      {
        return String.format(PATTERN,
                             "RECENT",
                             name,
                             recentStats.getN(),
                             recentStats.getMin() * scale,
                             recentStats.getMean() * scale,
                             recentStats.getMax() * scale,
                             recentStats.getStandardDeviation() * scale);
      }
  }
