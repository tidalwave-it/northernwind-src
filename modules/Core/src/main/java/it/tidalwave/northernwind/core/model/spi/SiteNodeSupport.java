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
package it.tidalwave.northernwind.core.model.spi;

import javax.annotation.Nonnull;
import it.tidalwave.util.As;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.Getter;
import lombok.experimental.Delegate;

/***********************************************************************************************************************
 *
 * A partial implementation of (@link SiteNode}.
 *
 * @author Fabrizio Giudici
 *
 **********************************************************************************************************************/
public abstract class SiteNodeSupport implements SiteNode
  {
    @Nonnull
    protected final ModelFactory modelFactory;

    @Getter @Delegate(excludes = As.class) @Nonnull
    private final Resource resource;

    @Delegate
    private final As as = As.forObject(this);

    public SiteNodeSupport (@Nonnull final ModelFactory modelFactory, @Nonnull final ResourceFile file)
      {
        this.modelFactory = modelFactory;
        this.resource = modelFactory.createResource().withFile(file).build();
      }
  }
