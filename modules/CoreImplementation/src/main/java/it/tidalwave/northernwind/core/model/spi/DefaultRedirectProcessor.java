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
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.emptyList;
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
            final var parts = configuration.split(" -> ");
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

    /* VisibleForTesting */ static final Key<List<String>> P_TEMPORARY_REDIRECTS = new Key<>("temporaryRedirects") {};

    @Inject
    private Provider<SiteProvider> siteProvider;

    private Site site;

    private final List<Mapping> permanentMappings = new ArrayList<>();

    private final List<Mapping> temporaryMappings = new ArrayList<>();

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
        final var rootSiteNode = site.find(_SiteNode_).withRelativeUri("/").result();
        final var rootSiteNodeProperties = rootSiteNode.getProperties();
        final var properties = rootSiteNodeProperties.getGroup(P_GROUP_ID);

        for (final var permanentRedirectConfig : properties.getProperty(P_PERMANENT_REDIRECTS).orElse(emptyList()))
          {
            permanentMappings.add(new Mapping(permanentRedirectConfig));
          }

        for (final var temporaryRedirectConfig : properties.getProperty(P_TEMPORARY_REDIRECTS).orElse(emptyList()))
          {
            temporaryMappings.add(new Mapping(temporaryRedirectConfig));
          }

        log.info("Permanent redirect mappings:");

        for (final var mapping : permanentMappings)
          {
            log.info(">>>> {}", mapping);
          }

        log.info("Temporary redirect mappings:");

        for (final var mapping : temporaryMappings)
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

            final var relativeUri = request.getRelativeUri();

            for (final var mapping : permanentMappings)
              {
                final var newRelativeUri = mapping.replace(relativeUri);

                if (!newRelativeUri.equals(relativeUri))
                  {
                    log.info(">>>> permanently redirecting from {} to {} ...", relativeUri, newRelativeUri);
                    throw HttpStatusException.permanentRedirect(site, newRelativeUri);
                  }
              }

            for (final var mapping : temporaryMappings)
              {
                final var newRelativeUri = mapping.replace(relativeUri);

                if (!newRelativeUri.equals(relativeUri))
                  {
                    log.info(">>>> temporarily redirecting from {} to {} ...", relativeUri, newRelativeUri);
                    throw HttpStatusException.temporaryRedirect(site, newRelativeUri);
                  }
              }
          }

        return Status.CONTINUE;
      }
  }
