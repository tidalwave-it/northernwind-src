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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.spi.LinkPostProcessor;

/***********************************************************************************************************************
 *
 * Since for some unexplainable reason @Inject doesn't allow an optional attribute, we must have at least one
 * {@code LinkPostProcessor} or {@code DefaultSite} fails injection.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class VoidLinkPostProcessor implements LinkPostProcessor
  {
    @Override @Nonnull
    public String postProcess (@Nonnull final String link)
      {
        return link;
      }
  }
