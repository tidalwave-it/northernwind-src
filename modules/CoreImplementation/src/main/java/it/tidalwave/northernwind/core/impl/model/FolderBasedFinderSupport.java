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
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.SimpleFinderSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteProvider;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import it.tidalwave.northernwind.core.model.ResourcePath;
import static it.tidalwave.northernwind.core.model.Resource.*;

/***********************************************************************************************************************
 *
 *
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction = true) @Slf4j @ToString
public class FolderBasedFinderSupport<Type extends Resource> extends SimpleFinderSupport<Type>
  {
    private static final long serialVersionUID = 2345536092354546452L;

    @Inject @Nonnull
    private transient Provider<SiteProvider> siteProvider;

    @Nonnull
    private final Class<Type> typeClass;

    @Nonnull
    private final ResourceFile parentFile;

    @Nonnull
    private final ResourcePath resourceRootPath;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @SuppressWarnings("unchecked")
    public FolderBasedFinderSupport (final @Nonnull Type parentResource)
      {
        try
          {
            final Site site = siteProvider.get().getSite();
            this.typeClass = (Class<Type>)parentResource.getClass().getInterfaces()[0]; // FIXME assumes the interesting interface is [0]
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
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected List<? extends Type> computeResults()
      {
        final List<Type> result = new ArrayList<>();

        for (final ResourceFile childFile : parentFile.getChildren(true))
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
  }
