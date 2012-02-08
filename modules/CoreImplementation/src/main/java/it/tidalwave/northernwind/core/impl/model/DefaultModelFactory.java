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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.openide.filesystems.FileObject;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.impl.ui.DefaultLayout;
import it.tidalwave.northernwind.frontend.ui.Layout;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ModelFactory}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString
public class DefaultModelFactory implements ModelFactory
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource createResource (final @Nonnull FileObject file)
      {
        return new DefaultResource(file);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Content createContent (final @Nonnull FileObject folder) 
      {
        return new DefaultContent(folder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Media createMedia (final @Nonnull FileObject file) 
      {
        return new DefaultMedia(file);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNode createSiteNode (final @Nonnull Site site, final @Nonnull FileObject folder) 
      throws IOException, NotFoundException
      {
        return new DefaultSiteNode((DefaultSite)site, folder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout createLayout (final @Nonnull Id id, final @Nonnull String type) 
      {
        return new DefaultLayout(id, type);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultRequest createRequest()
      {
        return new DefaultRequest("", "", "", new HashMap<String, List<String>>(), new ArrayList<Locale>());  
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Request createRequestFrom (final @Nonnull HttpServletRequest httpServletRequest)
      {
        String relativeUri = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
        relativeUri = relativeUri.equals("") ? "/" : relativeUri;
        return createRequest().withBaseUrl(getBaseUrl(httpServletRequest))
                              .withRelativeUri(relativeUri)
                              .withParameterMap(httpServletRequest.getParameterMap())
                              .withPreferredLocales(Collections.list(httpServletRequest.getLocales())); 
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties createProperties (final @Nonnull Id id) 
      {
        return new DefaultResourceProperties(id, null);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String getBaseUrl (final @Nonnull HttpServletRequest httpServletRequest)
      {
        return httpServletRequest.getRequestURL().toString().replaceAll(":.*", "") + "://" + httpServletRequest.getHeader("Host");
      }
  }
