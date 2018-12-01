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
 * An implementation of {@link Filter} based on regular expressions. Each instance of text which matches a given regular
 * expression is replaced with the result of the call to {@link #doFilter(Matcher)}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @ThreadSafe @Slf4j
public class MacroFilter implements Filter // FIXME: rename to RegexFilter
  {
    @Nonnull
    private final Pattern pattern;

    /*******************************************************************************************************************
     *
     * Creates an instance with the given regular expression.
     *
     * @param   regex     the regular expression
     *
     ******************************************************************************************************************/
    public MacroFilter (final @Nonnull String regex)
      {
        pattern = Pattern.compile(regex);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
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

    /*******************************************************************************************************************
     *
     * Apply the filtering given an instance of {@link Matcher}.
     *
     * @param   matcher   the {@code Matcher}
     * return             the filtered string
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      throws NotFoundException, IOException
      {
        return "";
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
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
