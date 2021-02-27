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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import it.tidalwave.role.Composite.VisitorSupport;
import it.tidalwave.northernwind.frontend.ui.Layout;
import org.slf4j.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NotThreadSafe @RequiredArgsConstructor @Slf4j
public class LayoutLoggerVisitor extends VisitorSupport<Layout, Object>
  {
    public static enum Level
      {
        DEBUG
          {
            @Override
            protected void log (final @Nonnull Logger log,
                                final @Nonnull String template,
                                final @Nonnull Object arg1,
                                final @Nonnull Object arg2)
              {
                log.debug(template, arg1, arg2);
              }
          },
        INFO
          {
            @Override
            protected void log (final @Nonnull Logger log,
                                final @Nonnull String template,
                                final @Nonnull Object arg1,
                                final @Nonnull Object arg2)
              {
                log.info(template, arg1, arg2);
              }
          };

        protected abstract void log (@Nonnull Logger log,
                                     @Nonnull String template,
                                     @Nonnull Object arg1,
                                     @Nonnull Object arg2);
      }

    private static final String SPACES = "                                                               ";

    private int indent = 0;

    @Nonnull
    private final Level logLevel;

    @Override
    public void preVisit (final @Nonnull Layout layout)
      {
        logLevel.log(log, "{}{}", SPACES.substring(0, indent++ * 2), layout);
      }

    @Override
    public void postVisit (final @Nonnull Layout layout)
      {
        indent--;
      }

    @Override
    public Object getValue()
      {
        return new Object();
      }
  }
