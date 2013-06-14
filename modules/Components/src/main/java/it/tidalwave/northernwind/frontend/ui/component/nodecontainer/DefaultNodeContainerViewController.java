/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.nodecontainer;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Collections;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteFinder.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import static it.tidalwave.northernwind.core.model.SiteNode.SiteNode;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultNodeContainerViewController implements NodeContainerViewController
  {
    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    private final Predicate<SiteNode> createRssLink = new Predicate<SiteNode>()
      {
        private final static String RSS_MIME_TYPE = "application/rss+xml";

        private final StringBuilder builder = new StringBuilder();

        @Override
        public boolean apply (final @Nonnull SiteNode rssSiteNode)
          {
            try
              {
                final String title = rssSiteNode.getProperties().getProperty(PROPERTY_TITLE, "RSS");
                builder.append(String.format("<link rel=\"alternate\" type=\"%s\" title=\"%s\" href=\"%s\" />%n",
                                             RSS_MIME_TYPE, title, site.createLink(rssSiteNode.getRelativeUri())));
              }
            catch (IOException e)
              {
                log.warn("", e); // shouldn't occur
              }

            return false;
          }

        @Override @Nonnull
        public String toString()
          {
            return builder.toString();
          }
      };

    protected static final String LINK_RELSTYLESHEET_MEDIASCREEN_HREF =
            "<link rel=\"stylesheet\" media=\"screen\" href=\"%s\" type=\"text/css\" />\n";

    @Nonnull
    private final NodeContainerView view;

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final Site site;

    @Nonnull
    private final RequestLocaleManager requestLocaleManager;

    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      throws IOException, NotFoundException
      {
        final ResourceProperties viewProperties = getViewProperties();
        final ResourceProperties siteNodeProperties = siteNode.getProperties();

        setCustomTemplate(viewProperties);
        view.addAttribute("language", requestLocaleManager.getLocales().get(0).getLanguage());
        view.addAttribute("titlePrefix", viewProperties.getProperty(PROPERTY_TITLE_PREFIX, ""));
        view.addAttribute("description", viewProperties.getProperty(PROPERTY_DESCRIPTION, ""));
        view.addAttribute("title", siteNodeProperties.getProperty(PROPERTY_TITLE, ""));
        view.addAttribute("screenCssSection", computeScreenCssSection());
        view.addAttribute("printCssSection", computePrintCssSection());
        view.addAttribute("rssFeeds", computeRssFeedsSection());
        view.addAttribute("scripts", computeScriptsSection());
        view.addAttribute("inlinedScripts", computeInlinedScriptsSection());
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
        final StringBuilder builder = new StringBuilder();

        try
          {
            for (final String relativeUri : getViewProperties().getProperty(PROPERTY_SCREEN_STYLE_SHEETS,
                                                                            Collections.<String>emptyList()))
              {
                final String link = relativeUri.startsWith("http") ? relativeUri
                                                                   : site.createLink(new ResourcePath(relativeUri));
                builder.append(String.format(LINK_RELSTYLESHEET_MEDIASCREEN_HREF, link));
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }

        return builder.toString();
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computePrintCssSection()
      {
        final StringBuilder builder = new StringBuilder();

        try
          {
            for (final String relativeUri : getViewProperties().getProperty(PROPERTY_PRINT_STYLE_SHEETS,
                                                                            Collections.<String>emptyList()))
              {
                final String link = relativeUri.startsWith("http") ? relativeUri
                                                                   : site.createLink(new ResourcePath(relativeUri));
                final String template = "<link rel=\"stylesheet\" media=\"print\" href=\"%s\" type=\"text/css\" />\n";
                builder.append(String.format(template, link));
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }

        return builder.toString();
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeRssFeedsSection()
      {
        try
          {
            for (final String relativePath : getViewProperties().getProperty(PROPERTY_RSS_FEEDS,
                                                                             Collections.<String>emptyList()))
              {
                site.find(SiteNode).withRelativePath(relativePath).doWithResults(createRssLink);
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }

        return createRssLink.toString();
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    private String computeScriptsSection()
      {
        final StringBuilder builder = new StringBuilder();

        try
          {
            for (final String relativeUri : getViewProperties().getProperty(PROPERTY_SCRIPTS,
                                                                            Collections.<String>emptyList()))
              {
                // Always use </script> to close, as some browsers break without
                builder.append(String.format("<script type=\"text/javascript\" src=\"%s\"></script>%n",
                                             site.createLink(new ResourcePath(relativeUri))));
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }

        return builder.toString();
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    private void setCustomTemplate (final @Nonnull ResourceProperties viewProperties)
      throws IOException
      {
        try
          {
            final String templateRelativePath = viewProperties.getProperty(PROPERTY_TEMPLATE_RESOURCE);
            final Content template = site.find(Content).withRelativePath(templateRelativePath).result();
            view.setTemplate(template.getProperties().getProperty(PROPERTY_TEMPLATE));
          }
        catch (NotFoundException e)
          {
            log.warn("Cannot find custom template, using default ({})", e.toString());
          }
      }

    /*******************************************************************************************************************
     *
     * .
     *
     ******************************************************************************************************************/
    @Nonnull
    protected String computeInlinedScriptsSection()
      {
        final StringBuilder builder = new StringBuilder();

        try
          {
            for (final String relativePath : getViewProperties().getProperty(PROPERTY_INLINED_SCRIPTS,
                                                                             Collections.<String>emptyList()))
              {
                try
                  {
                    final Content script = site.find(Content).withRelativePath(relativePath).result();
                    builder.append(script.getProperties().getProperty(PROPERTY_TEMPLATE));
                  }
                catch (NotFoundException e)
                  {
                    // ok, no script
                  }
              }
          }
        catch (IOException e)
          {
            log.error("", e);
          }

        return builder.toString();
      }
  }
