/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2013 by Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.htmltemplate;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlHolder extends TextHolder
  {
    /*******************************************************************************************************************
     *
     * Creates an instance with the given name.
     * 
     * @param  name  the component name
     *
     ******************************************************************************************************************/
    public HtmlHolder (final @Nonnull Id id) 
      {
        super(id);
        addAttribute("style", NW + id.stringValue());
        setMimeType("text/html");
      }
    
    public HtmlHolder (final @Nonnull String html) 
      {
        super(html);
        setMimeType("text/html");
      }
    
    public void setClassName (final @Nonnull String className)
      {
        addAttribute("style", className);
      }
  }
