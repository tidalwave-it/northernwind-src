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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.impl.model.Filter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @ThreadSafe @Slf4j
public class MacroFilter implements Filter
  {
    @Nonnull
    private final Pattern pattern;

    public MacroFilter (final @Nonnull String regexp)
      {
        pattern = Pattern.compile(regexp);
      }

    @Override @Nonnull
    public String filter (final @Nonnull String text, final @Nonnull String mimeType)
      {
        final Matcher matcher = pattern.matcher(text);
        final StringBuffer buffer = new StringBuffer();

        while (matcher.find())
          {
            final String filtered = doFilter(matcher);
            try
              {
                matcher.appendReplacement(buffer, filtered);
              }
            catch (IllegalArgumentException e) // better diagnostics
              {
                throw new IllegalArgumentException(String.format("Pattern error: %s regexp: %s%n**** filtered: %s%n**** buffer: %s",
                                                   e.getMessage(), pattern.pattern(), filtered, buffer));
              }
          }

        matcher.appendTail(buffer);

        return buffer.toString();
      }

    @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      throws NotFoundException, IOException
      {
        return "";
      }

    @Nonnull
    private String doFilter (final @Nonnull Matcher matcher)
      {
        try
          {
            return filter(matcher);
          }
        catch (NotFoundException | IOException e)
          {
            log.error("", e);
            return "";
          }
      }
  }
