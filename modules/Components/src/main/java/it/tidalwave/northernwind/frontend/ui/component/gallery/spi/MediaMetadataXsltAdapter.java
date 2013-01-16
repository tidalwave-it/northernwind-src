/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j
public class MediaMetadataXsltAdapter
  {
    private static MediaMetadataXsltAdapter instance;

    @Inject @Nonnull
    private ApplicationContext context;

    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;

    @Nonnull
    private MediaMetadataProvider mediaMetadataProvider;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String getMetadataString (final @Nonnull String id, final @Nonnull String format)
      {
        try
          {
            return getInstance().getMetadataString(new Id(id), format);
          }
        catch (Exception e)
          {
            log.error("During XSLT invocation", e);
            return "ERROR";
          }
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static synchronized MediaMetadataXsltAdapter getInstance()
      {
        if (instance == null)
          {
            instance = new MediaMetadataXsltAdapter();
          }

        return instance;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private MediaMetadataXsltAdapter()
      {
        mediaMetadataProvider = findMediaMetadataProvider();
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getMetadataString (final @Nonnull Id id, final @Nonnull String format)
      throws NotFoundException
      {
        // FIXME: should use current SiteNode properties
        final ResourceProperties properties = siteProvider.get().getSite().find(SiteNode.class)
                                                                          .withRelativePath("/")
                                                                          .result()
                                                                          .getProperties();
        return mediaMetadataProvider.getMetadataString(id, format, properties);
      }

    /*******************************************************************************************************************
     *
     * Finds the {@link MediaMetadataProvider}.
     *
     ******************************************************************************************************************/
    @Nonnull
    private MediaMetadataProvider findMediaMetadataProvider()
      {
        // FIXME: should be read by Node properties
        String metadataProviderName = "EmbeddedMediaMetadataProvider";

        try
          {
            return context.getBean(metadataProviderName, MediaMetadataProvider.class);
          }
        catch (NoSuchBeanDefinitionException e)
          {
            log.warn("Cannot find bean: {}", metadataProviderName);
            return MediaMetadataProvider.VOID;
          }
      }
  }
