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
package it.tidalwave.northernwind.frontend.ui.component.gallery;

import it.tidalwave.northernwind.core.model.ModifiablePath;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Id;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader.SlideShowProPlayerGalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerView;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultGalleryViewController extends DefaultNodeContainerViewController implements GalleryViewController
  {
    @Nonnull
    private final SiteNode siteNode;

    protected GalleryAdapter galleryAdapter;

    protected final List<Item> items = new ArrayList<Item>();

    protected final Map<Id, Item> itemMapById = new HashMap<Id, Item>();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultGalleryViewController (final @Nonnull NodeContainerView view,
                                         final @Nonnull SiteNode siteNode,
                                         final @Nonnull Site site,
                                         final @Nonnull RequestLocaleManager requestLocaleManager)
      {
        super(view, siteNode, site, requestLocaleManager);
        this.siteNode = siteNode;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    private void initialize()
      {
        loadItems(siteNode.getProperties());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<SiteNode> findChildrenSiteNodes()
      {
        return new SimpleFinderSupport<SiteNode>()
          {
            @Override
            protected List<? extends SiteNode> computeResults()
              {
                log.info("findChildrenSiteNodes()");
                final List<SiteNode> results = new ArrayList<SiteNode>();
                results.add(new ChildSiteNode(siteNode, siteNode.getRelativeUri().append("lightbox"), siteNode.getProperties()));

                for (final Item item : itemMapById.values())
                  {
                    final ModifiablePath relativeUri = siteNode.getRelativeUri().append(item.getId().stringValue());
                    results.add(new ChildSiteNode(siteNode, relativeUri, siteNode.getProperties()));
                  }

                log.info(">>>> returning: {}", results);

                return results;
              }
          };
      }

    /*******************************************************************************************************************
     *
     * Loads the items in the gallery.
     *
     ******************************************************************************************************************/
    private void loadItems (final @Nonnull ResourceProperties properties)
      {
        final long time = System.currentTimeMillis();
        items.clear();
        itemMapById.clear();
        final GalleryLoader loader = new SlideShowProPlayerGalleryLoader(properties); // FIXME: make it configurable
        items.addAll(loader.loadGallery(siteNode));

        for (final Item item : items)
          {
            itemMapById.put(item.getId(), item);
          }

        log.info("gallery items loaded in {} msec", System.currentTimeMillis() - time);
      }
  }
