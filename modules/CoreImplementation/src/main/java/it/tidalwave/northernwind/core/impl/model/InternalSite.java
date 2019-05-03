/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2019 Tidalwave s.a.s. (http://tidalwave.it)
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
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.Site;

/***********************************************************************************************************************
 *
 * Extension of {@link Site} with a few methods requires by the implementations.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
// FIXME: drop it
// isLogConfiguraitonEnabled() can be pushed to Site
// getNodeFolder() can be site.find(SiteNode.class).withRelativePath("/").result().getFile();

public interface InternalSite extends Site
  {
    @Nonnull
    public ResourceFile getNodeFolder();

    public boolean isLogConfigurationEnabled();
  }
