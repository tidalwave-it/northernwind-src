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
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import it.tidalwave.util.spi.HierarchicFinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import static it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory.*;
import static it.tidalwave.northernwind.core.model.Content._Content_;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * A mock implementation of {@link SiteFinder<Content>} which must be called with
 * {@link #withRelativePath(java.lang.String)}. The typical initialisation is:
 *
 * <pre>
        Site site = mock(Site.class);
        MockContentSiteFinder.registerTo(site);
 * </pre>
 *
 * It will return mock {@link Content} instances for any relative path that doesn't contain {@code "nonexistent"}. It's
 * guaranteed that the same mocked instance is always returned for any path, thus they can be stubbed which code like
 * this:
 *
 * <pre>
 *      // given
 *      Content content = site.find(Content).withRelativePath("/test/path").result();
 *      when(content.someMethod()).thenReturn(...); // stub the content
 *      ...
 *      // when
 *      //   run some method that retrieves a Content mapped to /test/path
 * </pre>
 *
 * Mocked instance of {@code Content} are also bound to mocked {@code ResourceProperties} instances, so the following
 * code is valid too:
 *
 * <pre>
 *      // given
 *      Content content = site.find(Content).withRelativePath("/test/path").result();
 *      when(content.getProperties().getProperty(eq(...))).thenReturn("some value");
 * </pre>
 *
 * Note: this mock has got an implementation that is too complex. But this project is used on didactic purposes too, and
 * it makes sense to have some imperfect stuff from the real world too.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, staticName = "fields")
public class MockContentSiteFinder extends HierarchicFinderSupport<Content, SiteFinder<Content>>
                                   implements SiteFinder<Content>
  {
    private static final long serialVersionUID = 1L;

    private final Site site;

    private final String relativePath;

    private final String relativeUri;

    // This makes it sure that different runs with different mocked Sites use different data
    private static final Map<Site, Map<String, Content>> SITE_CACHE = new IdentityHashMap<>();

    public static void registerTo (@Nonnull final Site site)
      {
        when(site.find(eq(_Content_))).thenReturn(new MockContentSiteFinder(site, null, null));
      }

    public MockContentSiteFinder (@Nonnull final MockContentSiteFinder other, @Nonnull final Object override)
      {
        super(other, override);
        final var source = getSource(MockContentSiteFinder.class, other, override);
        this.site         = source.site;
        this.relativePath = source.relativePath;
        this.relativeUri  = source.relativeUri;
      }

    @Override @Nonnull
    public SiteFinder<Content> withRelativePath (@Nonnull final String relativePath)
      {
        return clonedWith(fields(site, relativePath, relativeUri));
      }

    @Override @Nonnull
    public SiteFinder<Content> withRelativeUri (@Nonnull final String relativeUri)
      {
        return clonedWith(fields(site, relativePath, relativeUri));
      }

    @Override @Nonnull
    protected List<Content> computeResults()
      {
        assert relativePath != null : "relativePath is null";

        if (relativePath.contains("nonexistent"))
          {
            return Collections.emptyList();
          }

        final var contentMapByRelativePath = SITE_CACHE.computeIfAbsent(site, __ -> new HashMap<>());
        return List.of(contentMapByRelativePath.computeIfAbsent(relativePath, MockContentSiteFinder::createMockContentWithPath));
      }

    @Nonnull
    private static Content createMockContentWithPath (@Nonnull final String relativePath)
      {
        final var content = createMockContent();
        final var properties = createMockProperties();
        when(content.getProperties()).thenReturn(properties);
        when(content.getExposedUri()).thenReturn(Optional.of(mockedExposedUri(relativePath)));

        return content;
      }

    // TODO: this could be injected as a function, so only some specific tests use it
    @Nonnull
    private static ResourcePath mockedExposedUri (@Nonnull final String relativePath)
      {
        return "/".equals(relativePath)
                ? ResourcePath.EMPTY
                : ResourcePath.of("EXPOSED-" + relativePath.substring(1).replace('/', '-').replace(' ', '-'));
      }
  }
