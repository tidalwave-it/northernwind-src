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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.springframework.core.annotation.Order;
import it.tidalwave.northernwind.core.impl.model.Filter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Order(HIGHEST_PRECEDENCE + 10) @Slf4j
public class HtmlCleanupFilter implements Filter
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String filter (@Nonnull final String text, @Nonnull final String mimeType)
      {
        try
          {
            return "text/html".equals(mimeType) || "application/xhtml+xml".equals(mimeType) ? formatHtml(text) : text;
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static String formatHtml (@Nonnull final String text)
      throws IOException
      {
        final var sw = new StringWriter();
        final var br = new BufferedReader(new StringReader(text));

        var inBody = false;

        for (;;)
          {
            final var s = br.readLine();

            if (s == null)
              {
                break;
              }

            if (s.contains("<!-- @nw.HtmlCleanupFilter.enabled=false"))
              {
                return text;
              }

            if ("</body>".equals(s.trim()))
              {
                break;
              }

            if (inBody)
              {
                sw.write(s + "\n");
              }

            if ("<body>".equals(s.trim()))
              {
                inBody = true;
              }
          }

        sw.close();
        br.close();

        return inBody ? sw.getBuffer().toString() : text;
      }
  }
