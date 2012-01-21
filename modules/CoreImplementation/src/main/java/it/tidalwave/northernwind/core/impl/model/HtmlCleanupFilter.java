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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import static org.springframework.core.Ordered.*;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@Order(HIGHEST_PRECEDENCE) @Slf4j
public class HtmlCleanupFilter implements Filter
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String filter (final @Nonnull String text, final @Nonnull String mimeType) 
      {
        try
          {
            return mimeType.equals("text/html") ? formatHtml(text) : text;
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static String formatHtml (final @Nonnull String text) 
      throws IOException
      {
        final StringWriter sw = new StringWriter();
        final BufferedReader br = new BufferedReader(new StringReader(text));

        boolean inBody = false;

        for (;;)
          {
            final String s = br.readLine();

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
