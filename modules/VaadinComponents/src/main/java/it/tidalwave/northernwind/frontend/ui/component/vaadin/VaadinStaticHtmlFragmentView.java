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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.vaadin;

import javax.annotation.Nonnull;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.frontend.ui.component.StaticHtmlFragmentView;
import com.vaadin.ui.Label;
import lombok.Getter;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class VaadinStaticHtmlFragmentView extends Label implements StaticHtmlFragmentView
  {
    @Getter @Nonnull
    private final Id id;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given name.
     * 
     * @param  id  the component name
     *
     ******************************************************************************************************************/
    public VaadinStaticHtmlFragmentView (final @Nonnull Id id) 
      {
        this.id = id;
        setStyleName(NW + id.stringValue());
        setContentMode(Label.CONTENT_RAW);
      }
    
    @Override
    public void setContent (final @Nonnull String html) 
      {
        setValue(html);
      }    
  }
