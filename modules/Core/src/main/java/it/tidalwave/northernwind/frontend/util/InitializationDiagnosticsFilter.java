/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.util;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.util.InitializationDiagnosticsServletContextListenerDecorator.*;

/***********************************************************************************************************************
 *
 * A Servlet Filter that shows an error page when an error occurred during Spring boot. 
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class InitializationDiagnosticsFilter implements Filter 
  {
    private FilterConfig filterConfig = null;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void init (final @Nonnull FilterConfig filterConfig)
      {        
        this.filterConfig = filterConfig;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void destroy() 
      {
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void doFilter (final @Nonnull ServletRequest request, 
                          final @Nonnull ServletResponse response, 
                          final @Nonnull FilterChain chain)
      throws IOException, ServletException 
      {
        final Throwable bootThrowable = (Throwable)filterConfig.getServletContext().getAttribute(ATTRIBUTE_BOOT_THROWABLE);
        
        if (bootThrowable != null)
          {
            sendProcessingError(findUpperCauseWithMessage(bootThrowable), response);                
          }
        else
          {
            chain.doFilter(request, response);                
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Throwable findUpperCauseWithMessage (final @Nonnull Throwable t)
      {
        Throwable cause = t;

        for (Throwable parent = cause.getCause(); parent != null; parent = parent.getCause())
          {
            final String message = parent.getMessage();

            if ((message != null) && !"".equals(message.trim()))
              {
                cause = parent;
              } 
          }
        
        return cause;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void sendProcessingError (final @Nonnull Throwable t, final @Nonnull ServletResponse response)
      throws IOException
      {
        response.setContentType("text/html");
        final PrintWriter pw = new PrintWriter(new PrintStream(response.getOutputStream()));                
        pw.print("<html>\n<head>\n<title>Configuration Error</title>\n</head>\n<body>\n"); //NOI18N
        pw.print("<h1>Configuration Error</h1>\n<pre>\n");                
        pw.print(t.getMessage());                
        pw.print("</pre></body>\n</html>");
        pw.close();
        response.getOutputStream().close();
      }
  }
