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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;
import java.util.regex.Pattern;
import java.text.Normalizer;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Finder8;
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
import static it.tidalwave.northernwind.core.impl.model.DefaultContent.*;

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
      }

    @Nonnull @Delegate(types=ResourceProperties.class, excludes=Exclusions.class)
    private final ResourceProperties delegate;

    @Override
    public <T> Optional<T> getProperty (final @Nonnull Key<T> key)
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
 * @version $Id$
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
    public DefaultContent (final @Nonnull Content.Builder builder)
      {
        super(builder);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder8<Content> findChildren()
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
        final Optional<String> exposedUri = getProperty(PROPERTY_EXPOSED_URI);

        return exposedUri.isPresent() ? exposedUri.map(ResourcePath::new)
                                      : getDefaultExposedUri();
      }

    // FIXME: this is declared in Frontend Components. Either move some properties in this module, or the next
    // method can't stay here.
    public static final Key<String> PROPERTY_TITLE = new Key<>("title");

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<ResourcePath> getDefaultExposedUri()
      {
        return getResource().getProperty(PROPERTY_TITLE)
            .map(this::deAccent)
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
            .map(ResourcePath::new);
      }

    /*******************************************************************************************************************
     *
     * See http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet
     *
     ******************************************************************************************************************/
    @Nonnull
    public String deAccent (final @Nonnull String string)
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
