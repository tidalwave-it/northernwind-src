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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import it.tidalwave.northernwind.frontend.ui.SiteViewController.HttpErrorException;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Controller @Slf4j
public class SpringMvcRestController 
  {
    @Inject @Nonnull
    private SiteViewController siteViewController;
    
    @Inject @Nonnull
    private ResponseEntityHolder responseHolder;

    @RequestMapping(value="/**", method=RequestMethod.GET) @Nonnull
    public ResponseEntity<?> get (final @Nonnull HttpServletRequest request)
      throws HttpErrorException, MalformedURLException, IOException
      {
        final String relativeUri = "/" + request.getRequestURI().substring(request.getContextPath().length() + 1);
        log.info("GET {}", relativeUri);
        
        try
          { 
            return siteViewController.processRequest(new URL("http://localhost:8080/"), relativeUri); // FIXME
          }
        catch (HttpErrorException e)
          {
            return responseHolder.response().withContentType("text/plain")
                                            .withBody(e.getMessage())
                                            .withStatus(e.getStatusCode())
                                            .build();
          }
      }
  }