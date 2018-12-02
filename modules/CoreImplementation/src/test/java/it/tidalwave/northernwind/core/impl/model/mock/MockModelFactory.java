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
 * $Id: d8f4f618d7419e6126c0c7cd4f7de37777a26b83 $
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model.mock;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.spi.ModelFactorySupport;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id: d8f4f618d7419e6126c0c7cd4f7de37777a26b83 $
 *
 **********************************************************************************************************************/
@Slf4j
public class MockModelFactory extends ModelFactorySupport
  {
    @Getter
    private Map<String, String> resourceProperties = new HashMap<>();

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource build (final @Nonnull Resource.Builder builder)
      {
        final Resource resource = mock(Resource.class);
        when(resource.getProperty(any())).thenCallRealMethod();
        final String path = builder.getFile().getPath().asString();
        log.trace(">>>> creating Resource for {}", path);

        when(resource.toString()).thenReturn(String.format("Resource(path=%s)", path));
        return resource;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Content build (final @Nonnull Content.Builder builder)
      {
        final Content content = mock(Content.class);
        final ResourceProperties properties = createMockProperties();
        when(content.getProperties()).thenReturn(properties);
        when(content.getProperty(any())).thenCallRealMethod();
        final String path = builder.getFolder().getPath().asString();
        log.trace(">>>> creating Content for {}", path);

        when(content.toString()).thenReturn(String.format("Content(path=%s)", path));
        return content;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Media build (final @Nonnull Media.Builder builder)
      {
        final Media media = mock(Media.class);
        final String path = builder.getFile().getPath().asString();
        log.trace(">>>> creating Media for {}", path);

        when(media.toString()).thenReturn(String.format("Media(path=%s)", path));
        return media;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNode createSiteNode (final @Nonnull Site site, final @Nonnull ResourceFile folder)
            throws IOException, NotFoundException
      {
        final String relativeUri = String.format("relativeUriFor:%s", folder.getPath().asString());
        final String path = folder.getPath().asString();
        log.trace(">>>> creating SiteNode for {}", path);
        final SiteNode siteNode = mock(SiteNode.class);
        when(siteNode.getProperty(any())).thenCallRealMethod();
        when(siteNode.getRelativeUri()).thenReturn(new ResourcePath(relativeUri));
        when(siteNode.toString()).thenReturn(String.format("Node(path=%s)", path));

        final ResourceProperties properties = createMockProperties();
        when(siteNode.getProperties()).thenReturn(properties);

        // FIXME: drop this - instead find the required SiteNode and stub its properties; see comments of MockContentSiteFinder
        for (final Map.Entry<String, String> e : resourceProperties.entrySet())
          {
            if (e.getKey().startsWith(path + "."))
              {
                final String propertyName = e.getKey().substring(path.length() + 1);
                final Key<String> propertyKey = new Key<>(propertyName);
                log.trace(">>>>>>>> setting property {} = {}", propertyKey.stringValue(), e.getValue());
                when(properties.getProperty(eq(propertyKey))).thenReturn(Optional.of(e.getValue()));
              }
          }

        return siteNode;
      }

    @Nonnull
    private ResourceProperties createMockProperties()
      {
        final ResourceProperties properties = mock(ResourceProperties.class);
        when(properties.getProperty(any(Key.class))).thenReturn(Optional.empty()); // default
        return properties;
      }
  }
