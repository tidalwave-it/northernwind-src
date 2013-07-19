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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import it.tidalwave.util.spi.FinderSupport;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFile.Finder;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString(callSuper = true)
public class ResourceFileFinderSupport extends FinderSupport<ResourceFile, Finder> implements Finder
  {
    protected boolean recursive = false;

    @CheckForNull
    protected String name;

    protected ResourceFileFinderSupport()
      {
      }

    protected ResourceFileFinderSupport (final @Nonnull String name)
      {
        super(name);
      }

    @Override @Nonnull
    public Finder withRecursion (final boolean recursive)
      {
        final ResourceFileFinderSupport clone = (ResourceFileFinderSupport)clone();
        clone.recursive = recursive;
        return clone;
      }

    @Override @Nonnull
    public Finder withName (final @Nonnull String name)
      {
        final ResourceFileFinderSupport clone = (ResourceFileFinderSupport)clone();
        clone.name = name;
        return clone;
      }
  }

