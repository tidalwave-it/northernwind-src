/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.frontend.jersey;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Path("/")
public class RestResource
  {
    @Inject
    private SiteViewController siteViewController;

    @Inject
    private ModelFactory modelFactory;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    public Response getRoot()
      {
        return siteViewController.processRequest(modelFactory.createRequestFrom(httpServletRequest));
      }

    @GET @Path("{path: .*}")
    public Response get()
      {
        return siteViewController.processRequest(modelFactory.createRequestFrom(httpServletRequest));
      }
  }
