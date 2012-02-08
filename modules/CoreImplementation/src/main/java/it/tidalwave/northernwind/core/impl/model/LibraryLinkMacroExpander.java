/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.regex.Matcher;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteProvider;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class LibraryLinkMacroExpander extends MacroExpander
  {
    @Inject @Nonnull
    private SiteProvider siteProvider;
    
    public LibraryLinkMacroExpander()
      {
        super("\\$libraryLink\\(relativePath='([^']*)'\\)\\$");
      } 
    
    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      {
        try
          {
            final String relativePath = matcher.group(1);
            return siteProvider.getSite().createLink(relativePath);
          }
        catch (NotFoundException e)
          {
            return "";  
          }
      }
  }
