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
package it.tidalwave.northernwind.frontend.htmltemplate;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import it.tidalwave.northernwind.frontend.ui.SiteViewController.HttpErrorException;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Path("/") @Slf4j
public class RestResource 
  {
    @Inject @Nonnull
    private SiteViewController siteViewController;
    
    @Inject @Nonnull
    private ResponseThreadLocal responseHolder;
    
    @Context
    private UriInfo uriInfo;
    
    @GET
    public Response getRoot()
      throws HttpErrorException, MalformedURLException
      {
        return get("");
      }
    
    @GET @Path("{path: .*}") 
    public Response get()
      throws HttpErrorException, MalformedURLException
      {
        return get(uriInfo.getPath(true));
      }

    @Nonnull
    private Response get (final @Nonnull String relativeUri)
      throws HttpErrorException, MalformedURLException
      {
        responseHolder.set(null);
        log.info("GET /{}", relativeUri);
        
        try
          { 
            siteViewController.handleUri(new URL("http://localhost:8080/"), relativeUri); // FIXME
            return responseHolder.get();
          }
        catch (HttpErrorException e)
          {
            return Response.status(e.getStatusCode()).entity(e.getMessage()).build();
          }
      }
  }