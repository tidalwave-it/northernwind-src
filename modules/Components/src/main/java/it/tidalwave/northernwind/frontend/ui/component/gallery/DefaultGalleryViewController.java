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
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryAdapter;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.GalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.gallery.spi.loader.SlideShowProPlayerGalleryLoader;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.*;

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
    private static class VirtualSiteNodeFinder extends SimpleFinder8Support<SiteNode>
      {
        private static final long serialVersionUID = 1L;

        @Nonnull
        private final DefaultGalleryViewController controller;

        public VirtualSiteNodeFinder (final @Nonnull VirtualSiteNodeFinder other, final @Nonnull Object override)
          {
            super(other, override);
            final VirtualSiteNodeFinder source = getSource(VirtualSiteNodeFinder.class, other, override);
            this.controller = source.controller;
          }

        @Override @Nonnull
        protected List<? extends SiteNode> computeResults()
          {
            final SiteNode siteNode = controller.siteNode;
            final List<SiteNode> result = controller.itemMapById.values().stream()
                    .map(gallery -> createVirtualNode(siteNode, gallery.getId().stringValue()))
                    .collect(toList());
            result.add(0, createVirtualNode(siteNode, "lightbox"));
            return result;
          }

        @Nonnull
        private VirtualSiteNode createVirtualNode (final @Nonnull SiteNode siteNode, final String relativeUri)
          {
            return new VirtualSiteNode(siteNode,
                                       siteNode.getRelativeUri().appendedWith(relativeUri),
                                       siteNode.getProperties());
          }
      }

    @Nonnull
    private final SiteNode siteNode;

    @Nonnull
    private final BeanFactory beanFactory;

    @Nonnull
    protected GalleryAdapter galleryAdapter;

    protected final List<GalleryItem> items = new ArrayList<>();

    protected final Map<Id, GalleryItem> itemMapById = new HashMap<>();

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultGalleryViewController (final @Nonnull NodeContainerView view,
                                         final @Nonnull SiteNode siteNode,
                                         final @Nonnull RequestLocaleManager requestLocaleManager,
                                         final @Nonnull BeanFactory beanFactory)
      {
        super(view, siteNode, requestLocaleManager);
        this.siteNode = siteNode;
        this.beanFactory = beanFactory;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void initialize()
      throws Exception
      {
        super.initialize();
        log.info("initialize() - {}", siteNode.getRelativeUri());
        final long time = System.currentTimeMillis();
        final GalleryLoader loader = new SlideShowProPlayerGalleryLoader(beanFactory, siteNode.getProperties()); // FIXME: make it configurable
        items.addAll(loader.loadGallery(siteNode));
        itemMapById.putAll(items.stream().collect(toMap(GalleryItem::getId, i -> i)));
        log.info(">>>> {} gallery items loaded in {} msec", items.size(), System.currentTimeMillis() - time);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder8<SiteNode> findVirtualSiteNodes()
      {
        return new VirtualSiteNodeFinder(this);
      }
  }
