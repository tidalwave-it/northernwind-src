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
package it.tidalwave.northernwind.frontend.impl.ui;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.TreeMap;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spring.ClassScanner;
import it.tidalwave.northernwind.core.model.HttpStatusException;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.ViewFactory;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.util.NotFoundException.throwWhenNull;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ViewFactory}.
 *
 * @stereotype  Factory
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j @ToString
public class DefaultViewFactory implements ViewFactory
  {
    /* package */ final Map<String, ViewBuilder> viewBuilderMapByTypeUri = new TreeMap<>();

    @Getter @Setter
    private boolean logConfigurationEnabled;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ViewAndController createViewAndController (@Nonnull final String viewTypeUri,
                                                      @Nonnull final Id viewId,
                                                      @Nonnull final SiteNode siteNode)
      throws NotFoundException, HttpStatusException
      {
        final var viewBuilder = throwWhenNull(viewBuilderMapByTypeUri.get(viewTypeUri),
                                              String.format("Cannot find %s: available: %s",
                                                                    viewTypeUri, viewBuilderMapByTypeUri.keySet()));
        return viewBuilder.createViewAndController(viewId, siteNode);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize() // FIXME: gets called twice
      throws IllegalArgumentException, SecurityException
      {
        final var classScanner = new ClassScanner().withAnnotationFilter(ViewMetadata.class);

        for (final var viewClass : classScanner.findClasses())
          {
            final var viewMetadata = viewClass.getAnnotation(ViewMetadata.class);
            final var typeUri = viewMetadata.typeUri();
            final var viewBuilder = new ViewBuilder(viewClass, viewMetadata.controlledBy());
            viewBuilderMapByTypeUri.put(typeUri, viewBuilder);
          }

        if (logConfigurationEnabled)
          {
            logConfiguration();
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void logConfiguration()
      {
        log.info("View definitions:");
        viewBuilderMapByTypeUri.values().forEach(viewBuilder -> log.info(">>>> {}", viewBuilder));
      }
  }
