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
package it.tidalwave.northernwind.frontend.ui.component.gallery.spi;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j
public final class MediaMetadataXsltAdapter
  {
    private static MediaMetadataXsltAdapter instance;

    @Inject
    private ApplicationContext context;

    @Inject
    private Provider<SiteProvider> siteProvider;

    @Nonnull
    private final MediaMetadataProvider mediaMetadataProvider;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String getMetadataString (@Nonnull final String id, @Nonnull final String format)
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
    private String getMetadataString (@Nonnull final Id id, @Nonnull final String format)
      throws NotFoundException
      {
        // FIXME: should use current SiteNode properties
        final var properties = siteProvider.get().getSite().find(SiteNode.class)
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
        final var metadataProviderName = "EmbeddedMediaMetadataProvider";

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
