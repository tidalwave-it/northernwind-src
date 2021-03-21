/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.Order;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.Request;
import it.tidalwave.northernwind.core.model.RequestProcessor;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static it.tidalwave.northernwind.core.model.SiteNode._SiteNode_;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @Order(HIGHEST_PRECEDENCE+1)
public class DefaultRedirectProcessor implements RequestProcessor
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    static class Mapping
      {
        private final String regex;
        private final String replacement;

        public Mapping (@Nonnull final String configuration)
          {
            final String[] parts = configuration.split(" -> ");
            regex = parts[0];
            replacement = parts[1];
          }

        @Nonnull
        public String replace (@Nonnull final String string)
          {
            return string.replaceAll(regex, replacement);
          }

        @Override @Nonnull
        public String toString()
          {
            return regex + " -> " + replacement;
          }
      }

    /* VisibleForTesting */ static final Id P_GROUP_ID = new Id("Redirector");

    /* VisibleForTesting */ static final Key<List<String>> P_PERMANENT_REDIRECTS = new Key<>("permanentRedirects") {};

    @Inject
    private Provider<SiteProvider> siteProvider;

    private Site site;

    private final List<Mapping> permanentMappings = new ArrayList<>();

    private boolean initialized;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
//    @PostConstruct // FIXME: see NW-224
    /* VisibleForTesting */ void initialize()
            throws NotFoundException
      {
        site = siteProvider.get().getSite();
        final SiteNode rootSiteNode = site.find(_SiteNode_).withRelativeUri("/").result();
        final ResourceProperties rootSiteNodeProperties = rootSiteNode.getProperties();
        final ResourceProperties properties = rootSiteNodeProperties.getGroup(P_GROUP_ID);

        for (final String permanentRedirectConfig : properties.getProperty(P_PERMANENT_REDIRECTS).orElse(emptyList()))
          {
            permanentMappings.add(new Mapping(permanentRedirectConfig));
          }

        log.info("Permanent redirect mappings:");

        for (final Mapping mapping : permanentMappings)
          {
            log.info(">>>> {}", mapping);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Status process (@Nonnull final Request request)
      throws HttpStatusException
      {
        if (siteProvider.get().isSiteAvailable()) // FIXME no "/" SiteNode available at this point
          {
            synchronized (this) // FIXME: see NW-224
              {
                if (!initialized)
                  {
                    try
                      {
                        initialize();
                      }
                    catch (NotFoundException e)
                      {
                        throw new RuntimeException(e);
                      }

                    initialized = true;
                  }
              } // END FIXME

            final String relativeUri = request.getRelativeUri();

            for (final Mapping mapping : permanentMappings)
              {
                final String newRelativeUri = mapping.replace(relativeUri);

                if (!newRelativeUri.equals(relativeUri))
                  {
                    log.info(">>>> redirecting from {} to {} ...", relativeUri, newRelativeUri);
                    throw HttpStatusException.permanentRedirect(site, newRelativeUri);
                  }
              }
          }

        return Status.CONTINUE;
      }
  }
