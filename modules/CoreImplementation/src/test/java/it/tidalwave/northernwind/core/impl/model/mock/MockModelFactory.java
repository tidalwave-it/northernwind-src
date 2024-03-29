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
package it.tidalwave.northernwind.core.impl.model.mock;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.Template;
import it.tidalwave.northernwind.core.model.spi.ModelFactorySupport;
import it.tidalwave.northernwind.core.impl.text.St4TemplateFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mockito.ArgumentMatcher;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class MockModelFactory extends ModelFactorySupport
  {
    private final Map<String, Object> resourceProperties = new HashMap<>();

    public static interface PropertySetter
      {
        public <T> void setProperty (@Nonnull String path, @Nonnull Key<T> key, @Nonnull T value);
      }

    @Getter
    private final PropertySetter propertySetter = new PropertySetter()
      {
        @Override
        public <T> void setProperty (@Nonnull final String path, @Nonnull final Key<T> key, @Nonnull final T value)
          {
            resourceProperties.put(path + "." + key.getName(), value);
          }
      };

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Resource build (@Nonnull final Resource.Builder builder)
      {
        final var resource = createMockResource();
        final var path = builder.getFile().getPath().asString();
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
    public Content build (@Nonnull final Content.Builder builder)
      {
        final var content = createMockContent();
        final var properties = createMockProperties();
        when(content.getProperties()).thenReturn(properties);
        final var path = builder.getFolder().getPath().asString();
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
    public Media build (@Nonnull final Media.Builder builder)
      {
        final var media = mock(Media.class);
        final var path = builder.getFile().getPath().asString();
        log.trace(">>>> creating Media for {}", path);

        when(media.toString()).thenReturn(String.format("Media(path=%s)", path));
        return media;
      }

    @RequiredArgsConstructor(staticName = "of")
    static final class KeyMatcher<T> implements ArgumentMatcher<Key<T>>
      {
        @Nonnull
        private final String name;

        @Override
        public boolean matches (final Key<T> key)
          {
            return key != null && key.getName().equals(name);
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public SiteNode createSiteNode (@Nonnull final Site site, @Nonnull final ResourceFile folder)
      {
        final var relativeUri = String.format("relativeUriFor:%s", folder.getPath().asString());
        final var path = folder.getPath().asString();
        log.trace(">>>> creating SiteNode for {}", path);
        final var siteNode = createMockSiteNode(site);
        when(siteNode.getRelativeUri()).thenReturn(ResourcePath.of(relativeUri));
        when(siteNode.toString()).thenReturn(String.format("Node(path=%s)", path));

        final var properties = createMockProperties();
        when(siteNode.getProperties()).thenReturn(properties);

        // FIXME: drop this - instead find the required SiteNode and stub its properties; see comments of MockContentSiteFinder
        for (final var e : resourceProperties.entrySet())
          {
            if (e.getKey().startsWith(path + "."))
              {
                final var propertyName = e.getKey().substring(path.length() + 1);
                log.trace(">>>>>>>> setting property {} = {}", propertyName, e.getValue());
                // ResourceProperties index by key name, not (name, type)
                // FIXME This mocking got too complex
                when(properties.getProperty(argThat(KeyMatcher.of(propertyName)))).thenReturn(Optional.of(e.getValue()));
              }
          }

        return siteNode;
      }


    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static ResourceProperties createMockProperties()
      {
        final var properties = mock(ResourceProperties.class);
        when(properties.getProperty(any(Key.class))).thenReturn(Optional.empty()); // default
        when(properties.getProperty(any(List.class))).thenCallRealMethod();
        return properties;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static Resource createMockResource()
      {
        final var resource = mock(Resource.class);
        when(resource.getProperty(any(Key.class))).thenCallRealMethod();
        when(resource.getProperty(any(List.class))).thenCallRealMethod();
        return resource;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static Content createMockContent()
      {
        final var content = mock(Content.class);
        when(content.getProperty(any(Key.class))).thenCallRealMethod();
        when(content.getProperty(any(List.class))).thenCallRealMethod();
        return content;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static SiteNode createMockSiteNode (@Nonnull final Site site)
      {
        final var siteNode = mock(SiteNode.class);
        when(siteNode.getSite()).thenReturn(site);
        when(siteNode.getProperty(any(Key.class))).thenCallRealMethod();
        when(siteNode.getProperty(any(List.class))).thenCallRealMethod();
        return siteNode;
      }

    /*******************************************************************************************************************
     *
     * Creates a mock {@link Site} with working methods to retrieve a {@link Template}. It looks overly complex, but at
     * the moment is needed so we can test views with their default, embedded templates (it would be simple to just mock
     * the site to return the desired template, but we should duplicate the code to read the embedded template.
     *
     * @return      site        the {@code Site}
     *
     ******************************************************************************************************************/
    @Nonnull
    public static Site createMockSite()
      {
        final var site = mock(Site.class);
        when(site.createLink(any(ResourcePath.class))).thenAnswer(i ->
          {
            final ResourcePath path = i.getArgument(0);
            return "http://acme.com" + path.asString() + (path.getExtension().isEmpty() ? "/" : "");
          });
        when(site.getTemplate(any(Class.class), any(Optional.class), any(String.class))).then(i ->
                new St4TemplateFactory(i.getArgument(0), site).getTemplate(i.getArgument(1), i.getArgument(2)));
        when(site.getTemplate(any(Class.class), any(ResourcePath.class))).then(i ->
            new St4TemplateFactory(i.getArgument(0), site).getTemplate(i.getArgument(1)));
        return site;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> SiteFinder<T> createMockSiteFinder()
      {
//        try
          {
            final SiteFinder<T> finder = mock(SiteFinder.class);
//            when(finder.result()).thenThrow(new NotFoundException("mock finder"));
            when(finder.results()).thenReturn(emptyList());
            when(finder.optionalResult()).thenReturn(Optional.empty());
            when(finder.stream()).thenCallRealMethod();
            when(finder.withRelativePath(any(String.class))).thenReturn(finder);
            when(finder.withRelativeUri(any(String.class))).thenReturn(finder);
            when(finder.withRelativePath(any(ResourcePath.class))).thenCallRealMethod();
            when(finder.withRelativeUri(any(ResourcePath.class))).thenCallRealMethod();

            return finder;
          }
//        catch (NotFoundException e)
//          {
//            throw new RuntimeException(e); // never happens
//          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static <T> void mockViewProperty (@Nonnull final SiteNode siteNode,
                                             @Nonnull final Id viewId,
                                             @Nonnull final Key<T> propertyKey,
                                             @Nonnull final Optional<T> propertyValue)
      {
        var properties = siteNode.getPropertyGroup(viewId);

        if (properties == null) // in the real world can't happen, now it means not mocked yet
          {
            properties = createMockProperties();
            when(siteNode.getPropertyGroup(eq(viewId))).thenReturn(properties);
          }

        when(properties.getProperty(eq(propertyKey))).thenReturn(propertyValue);
      }
  }
