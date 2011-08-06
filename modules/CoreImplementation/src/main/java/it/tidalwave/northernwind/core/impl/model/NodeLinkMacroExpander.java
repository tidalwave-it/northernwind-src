/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.SiteNode;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class NodeLinkMacroExpander extends MacroExpander
  {
    public NodeLinkMacroExpander()
      {
        super("\\$nodeLink\\(relativePath=(/[^)]*)\\)\\$");
      } 
    
    @Override @Nonnull
    protected String filter (final @Nonnull Matcher matcher)
      {
        try 
          {
            final String relativePath = matcher.group(1);
            final SiteNode siteNode = site.find(SiteNode.class).withRelativePath(relativePath).result();
            return contextPath + siteNode.getRelativeUri();
          }
        catch (NotFoundException e) 
          {
            // FIXME
            return "";
          }
      }
  }
