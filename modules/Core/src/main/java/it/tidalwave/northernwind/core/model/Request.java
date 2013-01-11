/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import java.util.Locale;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable
public interface Request
  {
    @Nonnull
    public Request withRelativeUri (@Nonnull String relativeUri);

    @Nonnull
    public String getBaseUrl();

    @Nonnull
    public String getRelativeUri();

    @Nonnull
    public String getOriginalRelativeUri();

    @Nonnull
    public List<Locale> getPreferredLocales();

    @Nonnull
    public String getParameter (@Nonnull String parameterName)
      throws NotFoundException;

    @Nonnull
    public List<String> getMultiValuedParameter (@Nonnull String parameterName)
      throws NotFoundException;

    @Nonnull
    public String getPathParams (@Nonnull SiteNode siteNode);
  }
