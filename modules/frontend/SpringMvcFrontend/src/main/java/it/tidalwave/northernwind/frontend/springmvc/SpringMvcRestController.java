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
package it.tidalwave.northernwind.frontend.springmvc;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import java.io.IOException;
import java.net.URLDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import static org.springframework.web.bind.annotation.RequestMethod.*;

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
    private ModelFactory modelFactory;
    
    @RequestMapping(value="/**", method=GET) @Nonnull
    public ResponseEntity<?> get (final @Nonnull HttpServletRequest request)
      {
        return siteViewController.processRequest(modelFactory.createRequestFrom(request));
      }
    
    // FIXME: move to a separated bean
    @RequestMapping(value="/editor/**", method=POST) @Nonnull
    public ResponseEntity<?> post (final @Nonnull HttpServletRequest request) 
      throws IOException
      {
        final String contentRelativeUri = request.getRequestURI().replaceAll("^/editor", "");
        final String content = URLDecoder.decode(IOUtils.toString(request.getReader()), "UTF-8");
        log.info("EDITOR UPDATE {}", contentRelativeUri);
        log.info("EDITOR UPDATE {}", content);
        return new ResponseEntity<>("Done!", HttpStatus.OK); // FIXME: is it the correct return code?
      }
  }