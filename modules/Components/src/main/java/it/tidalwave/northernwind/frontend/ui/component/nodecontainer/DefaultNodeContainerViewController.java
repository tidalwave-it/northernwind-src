/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;
import it.tidalwave.util.Key;
import java.util.Optional;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.RenderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static it.tidalwave.northernwind.core.model.Content.*;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class DefaultNodeContainerViewController implements NodeContainerViewController
  {
    // TODO: this class should not set the HTML contents... it should be a responsibility of the view.
    // Instead, it should pass unormatted objects
    private static final String RSS_MIME_TYPE = "application/rss+xml";

    private static final String TEMPLATE_LINK_SCREEN_CSS =
            "<link rel=\"stylesheet\" media=\"screen\" href=\"%s\" type=\"text/css\" />%n";

    private static final String TEMPLATE_LINK_PRINT_CSS =
            "<link rel=\"stylesheet\" media=\"print\" href=\"%s\" type=\"text/css\" />%n";

    // Always use </script> to close, as some browsers break without
    private static final String TEMPLATE_SCRIPT =
            "<script type=\"text/javascript\" src=\"%s\"></script>%n";

    private static final String TEMPLATE_RSS_LINK =
            "<link rel=\"alternate\" type=\"%s\" title=\"%s\" href=\"%s\" />%n";

    @Nonnull
    private final NodeContainerView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final RequestLocaleManager requestLocaleManager;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void renderView (final @Nonnull RenderContext context)
      throws Exception
      {
        final ResourceProperties viewProperties     = getViewProperties();
        final ResourceProperties siteNodeProperties = context.getRequestContext().getNodeProperties();
        viewProperties.getProperty(P_TEMPLATE_PATH).flatMap(p -> siteNode.getSite().getTemplate(getClass(), p))
                                                                                   .ifPresent(view::setTemplate);

        view.addAttribute("language",         requestLocaleManager.getLocales().get(0).getLanguage());
        viewProperties.getProperty(P_DESCRIPTION).ifPresent(d -> view.addAttribute("description", d));
        viewProperties.getProperty(P_TITLE_PREFIX).ifPresent(p -> view.addAttribute("titlePrefix", p));
        Stream.of(siteNodeProperties.getProperty(PD_TITLE), siteNodeProperties.getProperty(P_TITLE)) // TODO: use multi-key
              .filter(Optional::isPresent)
              .map(Optional::get)
              .findFirst().ifPresent(s -> view.addAttribute("title", s));
        view.addAttribute("screenCssSection", computeScreenCssSection());
        view.addAttribute("printCssSection",  computePrintCssSection());
        view.addAttribute("rssFeeds",         computeRssFeedsSection());
        view.addAttribute("scripts",          computeScriptsSection());
        view.addAttribute("inlinedScripts",   computeInlinedScriptsSection());
        siteNodeProperties.getProperty(PD_IMAGE_ID).ifPresent(id -> view.addAttribute("imageId", id));
        siteNodeProperties.getProperty(PD_URL).ifPresent(id -> view.addAttribute("url", id));
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResourceProperties getViewProperties()
      {
        return siteNode.getPropertyGroup(view.getId());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScreenCssSection()
      {
        return streamOf(P_SCREEN_STYLE_SHEETS)
                .map(this::createLink)
                .map(link -> String.format(TEMPLATE_LINK_SCREEN_CSS, link))
                .collect(joining());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computePrintCssSection()
      {
        return streamOf(P_PRINT_STYLE_SHEETS)
                .map(this::createLink)
                .map(link -> String.format(TEMPLATE_LINK_PRINT_CSS, link))
                .collect(joining());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeRssFeedsSection()
      {
        final Site site = siteNode.getSite();
        return streamOf(P_RSS_FEEDS)
                .flatMap(relativePath -> site.find(SiteNode).withRelativePath(relativePath).stream())
                .map(node -> String.format(TEMPLATE_RSS_LINK,
                                           RSS_MIME_TYPE,
                                           node.getProperty(P_TITLE).orElse("RSS"),
                                           site.createLink(node.getRelativeUri())))
                .collect(joining());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScriptsSection()
      {
        return streamOf(P_SCRIPTS)
                .map(relativeUri -> siteNode.getSite().createLink(ResourcePath.of(relativeUri)))
                .map(link -> String.format(TEMPLATE_SCRIPT, link))
                .collect(joining());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String computeInlinedScriptsSection()
      {
        return streamOf(P_INLINED_SCRIPTS)
                .flatMap(path -> siteNode.getSite().find(Content).withRelativePath(path).stream())
                .flatMap(script -> script.getProperty(P_TEMPLATE).map(Stream::of).orElseGet(Stream::empty)) // FIXME: simplify in Java 9
                .collect(joining());
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private Stream<String> streamOf (final @Nonnull Key<List<String>> key)
      {
        return getViewProperties().getProperty(key).orElse(emptyList()).stream();
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String createLink (final @Nonnull String relativeUri)
      {
        return relativeUri.startsWith("http") ? relativeUri : siteNode.getSite().createLink(ResourcePath.of(relativeUri));
      }
  }
