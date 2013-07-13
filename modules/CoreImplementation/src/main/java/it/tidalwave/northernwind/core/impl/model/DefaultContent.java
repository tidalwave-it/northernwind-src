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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.regex.Pattern;
import java.text.Normalizer;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.As;
import it.tidalwave.util.Finder;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.role.spring.SpringAsSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.RequestContext;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Delegate;
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
        public <Type> Type getProperty(Key<Type> key) throws NotFoundException, IOException;
        public <Type> Type getProperty(Key<Type> key, Type defaultValue) throws IOException;
      }

    @Nonnull @Delegate(types=ResourceProperties.class, excludes=Exclusions.class)
    private final ResourceProperties delegate;

    @Override
    public <Type> Type getProperty (final @Nonnull Key<Type> key)
      throws NotFoundException, IOException
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

    @Override
    public <Type> Type getProperty (final @Nonnull Key<Type> key, final @Nonnull Type defaultValue)
      throws IOException
      {
        try
          {
            requestContext.setContent(content);
            return delegate.getProperty(key, defaultValue);
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
@Configurable @Slf4j @ToString(exclude="requestContext")
/* package */ class DefaultContent implements Content
  {
    interface Exclusions
      {
        public ResourceProperties getProperties();
      }

    @Nonnull
    private final ModelFactory modelFactory;

    @Inject @Nonnull
    private RequestContext requestContext;

    @Nonnull @Delegate(types = Resource.class, excludes = { As.class, Exclusions.class })
    private final Resource resource;

    @Delegate
    private final As asSupport = new SpringAsSupport(this);

    /*******************************************************************************************************************
     *
     * Creates a new {@code DefaultContent} with the given configuration file.
     *
     * @param   file   the configuration file
     *
     ******************************************************************************************************************/
    public DefaultContent (final @Nonnull Content.Builder builder)
      {
        this.modelFactory = builder.getModelFactory();
        resource = modelFactory.createResource().withFile(builder.getFolder()).build();
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Finder<Content> findChildren()
      {
        return new FolderBasedFinderSupport<Content>(this);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourcePath getExposedUri()
      throws NotFoundException, IOException
      {
        try
          {
            return new ResourcePath(getProperties().getProperty(PROPERTY_EXPOSED_URI));
          }
        catch (NotFoundException e)
          {
            return getDefaultExposedUri();
          }
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
    private ResourcePath getDefaultExposedUri()
      throws NotFoundException, IOException
      {
        String title = resource.getProperties().getProperty(PROPERTY_TITLE);
        title = deAccent(title);
        title = title.replaceAll(" ", "-")
                     .replaceAll(",", "")
                     .replaceAll("\\.", "")
                     .replaceAll(";", "")
                     .replaceAll("/", "")
                     .replaceAll("!", "")
                     .replaceAll("\\?", "")
                     .replaceAll(":", "")
                     .replaceAll("[^\\w-]*", "");
        return new ResourcePath(title.toLowerCase());
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
        return new ResourcePropertiesDelegate(requestContext, this, resource.getProperties());
      }
  }
