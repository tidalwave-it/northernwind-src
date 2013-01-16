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
package it.tidalwave.northernwind.frontend.ui.component.sitemap;

import it.tidalwave.northernwind.core.model.ResourceProperties;
import javax.annotation.Nonnull;
import it.tidalwave.util.Finder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.util.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import it.tidalwave.northernwind.core.model.ResourceFile;

/***********************************************************************************************************************
 *
 * Controllers which manage composite site nodes implement this interface. For instance, the controller of a gallery
 * should implement this interface and return the relative paths of all the media pages in the gallery; the controller
 * of a blog should implement this interface and return the relative paths of all the posts.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface CompositeSiteNodeController
  {
    @RequiredArgsConstructor @ToString
    public static class ChildSiteNode implements SiteNode
      {
        @Nonnull
        private final SiteNode parentSiteNode;

        @Getter @Nonnull
        private final String relativeUri;

        @Getter @Nonnull
        private final ResourceProperties properties;

        @Override
        public Layout getLayout()
          {
            throw new UnsupportedOperationException("Not supported.");
          }

        @Override
        public ResourceFile getFile()
          {
            throw new UnsupportedOperationException("Not supported.");
          }

        @Override
        public ResourceProperties getPropertyGroup (final @Nonnull Id id)
          {
            throw new UnsupportedOperationException("Not supported.");
          }

        @Override
        public boolean isPlaceHolder()
          {
            throw new UnsupportedOperationException("Not supported.");
          }
      }

    @Nonnull
    public Finder<SiteNode> findChildrenSiteNodes();
  }
