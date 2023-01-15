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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class ModelFactorySupport implements ModelFactory,
                                            Resource.Builder.CallBack,
                                            Content.Builder.CallBack,
                                            Media.Builder.CallBack,
                                            ResourceProperties.Builder.CallBack,
                                            Site.Builder.CallBack,
                                            Layout.Builder.CallBack
  {
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final Resource.Builder createResource()
      {
        return new Resource.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final Content.Builder createContent()
      {
        return new Content.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNode createSiteNode (@Nonnull final Site site, @Nonnull final ResourceFile folder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final Media.Builder createMedia()
      {
        return new Media.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Request createRequest()
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Request createRequestFrom (@Nonnull final HttpServletRequest hsr)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final Layout.Builder createLayout()
      {
        return new Layout.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final ResourceProperties.Builder createProperties()
      {
        return new ResourceProperties.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public final Site.Builder createSite()
      {
        return new Site.Builder(this, this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties build (@Nonnull final ResourceProperties.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Site build (@Nonnull final Site.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Layout build (@Nonnull final Layout.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource build (@Nonnull final Resource.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Content build (@Nonnull final Content.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Media build (@Nonnull final Media.Builder builder)
      {
        throw new UnsupportedOperationException("Not supported.");
      }
  }
