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
 * This postprocessor normalize links related to {@link SiteNode}s by making sure they end with a trailing slash (/).
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class NormalizedLinkPostProcessor implements LinkPostProcessor
  {
    @Override @Nonnull
    public String postProcess (@Nonnull final String link)
      {
        final var trailing = link.replaceAll("^.*/", "");

        if (!trailing.contains(".") && !link.contains("?") && !link.endsWith("/"))
          {
            return link + "/";
          }

        return link;
      }
  }
