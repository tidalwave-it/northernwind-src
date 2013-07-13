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

import javax.annotation.Nonnull;
import it.tidalwave.util.As;
import it.tidalwave.role.spring.SpringAsSupport;
import it.tidalwave.northernwind.core.model.Media;
import it.tidalwave.northernwind.core.model.ModelFactory;
import it.tidalwave.northernwind.core.model.Resource;
import lombok.Delegate;
import lombok.Getter;

/***********************************************************************************************************************
 *
 * A {@code DefaultMedia} item is a document that is served as-is, without any processing. It's typically an image or
 * such.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public abstract class MediaSupport implements Media
  {
    @Nonnull
    protected final ModelFactory modelFactory;

    @Nonnull @Getter @Delegate(types = Resource.class, excludes = As.class)
    private final Resource resource;

    @Delegate
    private final As asSupport = new SpringAsSupport(this);

    public MediaSupport (final @Nonnull Media.Builder builder)
      {
        this.modelFactory = builder.getModelFactory();
        resource = modelFactory.createResource().withFile(builder.getFile()).build();
      }
  }
