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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model.mock;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import com.google.common.base.Predicate;
import it.tidalwave.northernwind.core.impl.model.DefaultSiteFinder;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.SiteFinder;
import lombok.RequiredArgsConstructor;
import static org.mockito.Mockito.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class MockContentSiteFinder extends FinderSupport<Content, DefaultSiteFinder<Content>>
                                   implements SiteFinder<Content>
  {
    private final static long serialVersionUID = 1L;

    private final String relativePath;

    private final String relativeUri;

    public MockContentSiteFinder()
      {
        this.relativePath = null;
        this.relativeUri = null;
      }

    public MockContentSiteFinder (final @Nonnull MockContentSiteFinder other, final @Nonnull Object override)
      {
        super(other, override);
        final MockContentSiteFinder source = getSource(MockContentSiteFinder.class, other, override);
        this.relativePath = source.relativePath;
        this.relativeUri = source.relativeUri;
      }

    @Override @Nonnull
    public SiteFinder<Content> withRelativePath (final @Nonnull String relativePath)
      {
        return clone(new MockContentSiteFinder(relativePath, relativeUri));
      }

    @Override @Nonnull
    public SiteFinder<Content> withRelativeUri (final @Nonnull String relativeUri)
      {
        return clone(new MockContentSiteFinder(relativePath, relativeUri));
      }

    @Override @Nonnull
    protected List<? extends Content> computeResults()
      {
        try
          {
            final Content content = mock(Content.class);

            if (relativePath.equals("/"))
              {
                when(content.getExposedUri2()).thenReturn(new ResourcePath());
              }
            else
              {
                when(content.getExposedUri2()).thenReturn(new ResourcePath("EXPOSED-" + relativePath.substring(1)
                                                                                  .replace('/', '-')
                                                                                  .replace(' ', '-')));
              }

            return Arrays.asList(content);
          }
        catch (NotFoundException | IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    @Override
    public void doWithResults (final @Nonnull Predicate<Content> predicate)
      {
        throw new UnsupportedOperationException("Not supported.");
      }
  }
