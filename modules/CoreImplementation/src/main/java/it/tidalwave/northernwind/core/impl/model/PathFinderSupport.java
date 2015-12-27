/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 *
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString
public class PathFinderSupport<T extends Resource> extends SimpleFinderSupport<T>
  {
    private static final long serialVersionUID = 2345536092354546452L;

    private static final List<Class<?>> ALLOWED_TYPES =
            Arrays.<Class<?>>asList(Content.class, SiteNode.class, Media.class);

    @Inject
    private transient Provider<SiteProvider> siteProvider;

    @Nonnull
    private final Class<T> typeClass;

    @Nonnull
    private final ResourceFile parentFile;

    @Nonnull
    private final ResourcePath resourceRootPath;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @SuppressWarnings("unchecked")
    public PathFinderSupport (final @Nonnull T parentResource)
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            this.typeClass = getInterface(parentResource);
            this.resourceRootPath = site.find(typeClass).withRelativePath("/").result().getFile().getPath();
            this.parentFile = parentResource.getFile();
          }
        catch (NotFoundException e)
          {
            throw new RuntimeException(e); // never occurs
          }
      }

    /*******************************************************************************************************************
     *
     * Clone constructor.
     *
     ******************************************************************************************************************/
    public PathFinderSupport (final @Nonnull PathFinderSupport<T> other, final @Nonnull Object override)
      {
        super(other, override);
        final PathFinderSupport<T> source = getSource(PathFinderSupport.class, other, override);
        this.typeClass = source.typeClass;
        this.parentFile = source.parentFile;
        this.resourceRootPath = source.resourceRootPath;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    @Nonnull
    protected List<? extends T> computeResults()
      {
        final List<T> result = new ArrayList<>();

        for (final ResourceFile childFile : parentFile.findChildren().withRecursion(true).results())
          {
            if (childFile.isFolder())
              {
                try
                  {
                    final String relativeUri = childFile.getPath().relativeTo(resourceRootPath).urlDecoded().asString();
                    result.add(siteProvider.get().getSite().find(typeClass).withRelativePath(relativeUri).result());
                  }
                catch (NotFoundException e)
                  {
                    log.error("", e);
                  }
              }
          }

        return result;
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Class<T> getInterface (final @Nonnull T resource)
      {
        for (Class<?> type = resource.getClass(); type != null; type = type.getSuperclass())
          {
            for (final Class<?> interface_ : type.getInterfaces())
              {
                if (ALLOWED_TYPES.contains(interface_))
                  {
                    return (Class<T>)interface_;
                  }
              }
          }

        throw new IllegalArgumentException("Illegal type: " + resource + "; allowed must implement " + ALLOWED_TYPES);
      }
  }