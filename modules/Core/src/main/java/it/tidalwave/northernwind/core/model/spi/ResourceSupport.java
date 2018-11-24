/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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

import javax.annotation.Nonnull;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.experimental.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * A partial implementation of (@link Resource}.
 *
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class ResourceSupport implements Resource
  {
    @Nonnull
    protected final ModelFactory modelFactory;

    @Getter @Nonnull
    private final ResourceFile file;

    @Delegate
    private final As asSupport = new AsSupport(this);

    public ResourceSupport (final @Nonnull Resource.Builder builder)
      {
        this.modelFactory = builder.getModelFactory();
        this.file = builder.getFile();
      }

    @Override @Nonnull
    public final ResourceProperties getPropertyGroup (final @Nonnull Id id)
      {
        return getProperties().getGroup(id);
      }
  }
