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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.text.Normalizer;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.spi.ContentSupport;
import lombok.experimental.Delegate;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

// FIXME: reimplement with an Aspect
@RequiredArgsConstructor
class ResourcePropertiesDelegate implements ResourceProperties
  {
    @Nonnull
    private final RequestContext requestContext;

    @Nonnull
    private final Content content;

    interface Exclusions
      {
        public <T> Optional<T> getProperty (Key<T> key);
        public <T> Optional<T> getProperty (List<Key<T>> keys);
      }

    @Nonnull @Delegate(types=ResourceProperties.class, excludes=Exclusions.class)
    private final ResourceProperties delegate;

    @Nonnull
    @Override
    public <T> Optional<T> getProperty (@Nonnull final Key<T> key)
      {
        try
          {
            requestContext.setContent(content);
            return delegate.getProperty(key);
          }
        finally
          {
            requestContext.clearContent();
          }
      }
  }

/***********************************************************************************************************************
 *
 * A piece of content to be composed into a {@code Node}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(callSuper = true, exclude="requestContext")
/* package */ class DefaultContent extends ContentSupport
  {
    @Inject
    private RequestContext requestContext;

    /*******************************************************************************************************************
     *
     * Creates a new {@code DefaultContent} with the given {@link Content.Builder}.
     *
     * @param   builder   the builder
     *
     ******************************************************************************************************************/
    public DefaultContent (@Nonnull final Content.Builder builder)
      {
        super(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<Content> findChildren()
      {
        return new PathFinderSupport<>(this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Optional<ResourcePath> getExposedUri()
      {
        final Optional<String> exposedUri = getProperty(P_EXPOSED_URI);
        return exposedUri.isPresent() ? exposedUri.map(ResourcePath::of) : getDefaultExposedUri();
//        return getProperty(P_EXPOSED_URI).map(ResourcePath::of).orElseGet(() -> getDefaultExposedUri()); TODO
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<ResourcePath> getDefaultExposedUri()
      {
        return getResource().getProperty(P_TITLE)
            .map(DefaultContent::deAccent)
            .map(t -> t.replaceAll(" ", "-")
                       .replaceAll(",", "")
                       .replaceAll("\\.", "")
                       .replaceAll(";", "")
                       .replaceAll("/", "")
                       .replaceAll("!", "")
                       .replaceAll("\\?", "")
                       .replaceAll(":", "")
                       .replaceAll("[^\\w-]*", ""))
            .map(String::toLowerCase)
            .map(ResourcePath::of);
      }

    /*******************************************************************************************************************
     *
     * See http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet
     *
     ******************************************************************************************************************/
    @Nonnull
    public static String deAccent (@Nonnull final String string)
      {
        final String nfdNormalizedString = Normalizer.normalize(string, Normalizer.Form.NFD);
        final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
      }

    @Override @Nonnull
    public ResourceProperties getProperties()
      {
        return new ResourcePropertiesDelegate(requestContext, this, getResource().getProperties());
      }
  }
