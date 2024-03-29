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
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.northernwind.frontend.util.NorthernWindContextLoaderListener.ATTRIBUTE_BOOT_THROWABLE;

/***********************************************************************************************************************
 *
 * A decorator for a {@link HttpServlet} that returns an error diagnostic page when there are problems during the boot.
 *
 * A simple {@code Filter} wouldn't accomplish the job, since we need to prevent the delegate servlet from initializing
 * in case of error.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class InitializationDiagnosticsDispatcherServletDecorator extends HttpServlet
  {
    private final DispatcherServlet delegate = new DispatcherServlet();

    @Nullable
    private Throwable bootThrowable;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void init (@Nonnull final ServletConfig config)
      throws ServletException
      {
        super.init(config);

        bootThrowable = (Throwable)getServletContext().getAttribute(ATTRIBUTE_BOOT_THROWABLE);

        if (bootThrowable == null)
          {
            delegate.init(config);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    protected void service (@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response)
      throws ServletException, IOException
      {
        if (bootThrowable == null)
          {
            delegate.service(request, response);
          }
        else
          {
            sendProcessingError(findUpperCauseWithMessage(bootThrowable), response);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Throwable findUpperCauseWithMessage (@Nonnull final Throwable throwable)
      {
        var cause = throwable;

        for (var parent = cause.getCause(); parent != null; parent = parent.getCause())
          {
            final var message = parent.getMessage();

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
    private static void sendProcessingError (@Nonnull final Throwable t, @Nonnull final HttpServletResponse response)
      throws IOException
      {
        response.setStatus(500);
        response.setContentType("text/html");
        final var pw = new PrintWriter(new PrintStream(response.getOutputStream(), true, UTF_8));
        pw.print("<html>\n<head>\n<title>Configuration Error</title>\n</head>\n<body>\n");
        pw.print("<h1>Configuration Error</h1>\n<pre>\n");
        pw.print(t);
//        t.printStackTrace(pw);
        pw.print("</pre>\n");
        pw.print("<h2>Boot log</h2>\n<pre>\n");
        pw.print(BootLogger.getLogContent());
        pw.print("</pre></body>\n</html>");
        pw.close();
        response.getOutputStream().close();
      }
  }
