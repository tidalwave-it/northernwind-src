/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
import javax.inject.Provider;
import java.util.List;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.HierarchicFinderSupport;
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
import static java.util.stream.Collectors.toList;

/***********************************************************************************************************************
 *
 *
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString
public class PathFinderSupport<T extends Resource> extends HierarchicFinderSupport<T, PathFinderSupport<T>>
  {
    private static final long serialVersionUID = 2345536092354546452L;

    private static final List<Class<?>> ALLOWED_TYPES = List.of(Content.class, SiteNode.class, Media.class);

    @Inject
    private transient Provider<SiteProvider> siteProvider;

    @Nonnull
    private final Class<T> typeClass;

    @Nonnull
    private final transient ResourceFile parentFile;

    @Nonnull
    private final ResourcePath resourceRootPath;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @SuppressWarnings("squid:S00112")
    public PathFinderSupport (@Nonnull final T parentResource)
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
    public PathFinderSupport (@Nonnull final PathFinderSupport<T> other, @Nonnull final Object override)
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
        final Site site = siteProvider.get().getSite();
        return parentFile.findChildren().withRecursion(true).results().stream()
            .filter(ResourceFile::isFolder)
            .flatMap(c -> site.find(typeClass).withRelativePath(c.getPath().relativeTo(resourceRootPath).urlDecoded())
                              .results().stream())
            .collect(toList());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Class<T> getInterface (@Nonnull final T resource)
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
