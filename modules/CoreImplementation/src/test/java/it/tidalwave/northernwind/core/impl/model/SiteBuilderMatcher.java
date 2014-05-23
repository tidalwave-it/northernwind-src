/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.List;
import java.util.Locale;
import it.tidalwave.northernwind.core.model.Site;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/***********************************************************************************************************************
 *
 * A test {@link Matcher} for {@link Site.Builder}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Wither @ToString
public class SiteBuilderMatcher extends BaseMatcher<Site.Builder>
  {
    private String contextPath;
    private String documentPath;
    private String mediaPath;
    private String libraryPath;
    private String nodePath;
    private boolean configurationEnabled;
    private List<Locale> configuredLocales;
    private List<String> ignoredFolders;
    
    @Override
    public boolean matches (final @Nonnull Object item) 
      {
        // Test properties set in the Spring beans file
        final Site.Builder siteBuilder = (Site.Builder)item;

        return siteBuilder.getContextPath().equals(contextPath)
            && siteBuilder.getDocumentPath().equals(documentPath)
            && siteBuilder.getMediaPath().equals(mediaPath)
            && siteBuilder.getLibraryPath().equals(libraryPath)
            && siteBuilder.getNodePath().equals(nodePath)
            && siteBuilder.isLogConfigurationEnabled() == configurationEnabled
            && siteBuilder.getConfiguredLocales().equals(configuredLocales)
            && siteBuilder.getIgnoredFolders().equals(ignoredFolders);
        }

      @Override
      public void describeTo (final @Nonnull Description description) 
        {
          description.appendText(this.toString());
        }
    }
