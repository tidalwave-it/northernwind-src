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
import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * We can't log to the real thing, since we first need to compute the path of the logging file. Logging to the real
 * thing would instantiate the logging facility before we have a chance to configure it.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public final class BootLogger
  {
    private static final StringBuilder BUILDER = new StringBuilder();

    private final Class<?> owner;

    public void log (@Nonnull final String string)
      {
        @SuppressWarnings("squid:S106")
        final var s = String.format("%s: %s", owner.getSimpleName(), string);
        System.err.println(s);
        BUILDER.append(s).append("\n");
      }

    public void log (@Nonnull final Throwable t)
      {
        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        log(t + "\n" + sw);
      }

    @Nonnull
    public static String getLogContent()
      {
        return BUILDER.toString();
      }
  }
