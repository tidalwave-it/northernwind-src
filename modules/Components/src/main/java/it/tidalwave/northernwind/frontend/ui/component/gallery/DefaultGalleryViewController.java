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
package it.tidalwave.northernwind.frontend.ui.component.gallery;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.BeanFactory;
import it.tidalwave.util.Finder8;
import it.tidalwave.util.Id;
import it.tidalwave.util.spi.SimpleFinder8Support;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader.SlideShowProPlayerGalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultGalleryViewController extends DefaultNodeContainerViewController implements GalleryViewController
  {
    /*******************************************************************************************************************
     *
     * A {@link Finder8} which returns virtual {@link SiteNode}s representing the multiple contents served by the
     * {@link SiteNode} associated to this controller. This is typically used to create site maps.
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    private static class ChildrenVirtualNodeFinder extends SimpleFinder8Support<SiteNode>
      {
        private static final long serialVersionUID = 1L;

        @Nonnull
        private final DefaultGalleryViewController controller;

        public ChildrenVirtualNodeFinder (final @Nonnull ChildrenVirtualNodeFinder other, final @Nonnull Object override)
          {
            super(other, override);
            final ChildrenVirtualNodeFinder source = getSource(ChildrenVirtualNodeFinder.class, other, override);
            this.controller = source.controller;
          }

        @Override @Nonnull
        protected List<? extends SiteNode> computeResults()
          {
            final List<SiteNode> results = new ArrayList<>();
            final SiteNode siteNode = controller.siteNode;
            results.add(new ChildSiteNode(siteNode,
                                          siteNode.getRelativeUri().appendedWith("lightbox"),
                                          siteNode.getProperties()));

            for (final Item item : controller.itemMapById.values())
              {
                final ResourcePath relativeUri = siteNode.getRelativeUri().appendedWith(item.getId().stringValue());
                results.add(new ChildSiteNode(siteNode, relativeUri, siteNode.getProperties()));
              }

            return results;
          }
      }

    @Nonnull
    private final SiteNode siteNode;

    private final BeanFactory beanFactory;

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
                                         final @Nonnull RequestLocaleManager requestLocaleManager,
                                         final @Nonnull BeanFactory beanFactory)
      {
        super(view, siteNode, site, requestLocaleManager);
        this.siteNode = siteNode;
        this.beanFactory = beanFactory;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override
    public void initialize()
      throws Exception
      {
        super.initialize();
        loadItems(siteNode.getProperties());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder8<SiteNode> findChildrenSiteNodes()
      {
        return new ChildrenVirtualNodeFinder(this);
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
        final GalleryLoader loader = new SlideShowProPlayerGalleryLoader(beanFactory, properties); // FIXME: make it configurable
        items.addAll(loader.loadGallery(siteNode));

        for (final Item item : items)
          {
            itemMapById.put(item.getId(), item);
          }

        log.info("{} gallery items loaded in {} msec", items.size(), System.currentTimeMillis() - time);
      }
  }
