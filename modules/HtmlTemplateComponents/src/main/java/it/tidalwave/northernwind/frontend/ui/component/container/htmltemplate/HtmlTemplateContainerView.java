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
package it.tidalwave.northernwind.frontend.ui.component.container.htmltemplate;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.frontend.ui.annotation.ViewMetadata;
import it.tidalwave.northernwind.frontend.ui.component.htmlfragment.htmltemplate.HtmlTemplateHtmlFragmentView;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.DefaultNodeContainerViewController;
import it.tidalwave.northernwind.frontend.ui.component.nodecontainer.NodeContainerView;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ViewMetadata(typeUri="http://northernwind.tidalwave.it/component/Container", 
              controlledBy=DefaultNodeContainerViewController.class)
public class HtmlTemplateContainerView extends HtmlTemplateHtmlFragmentView implements NodeContainerView
  {
    /*******************************************************************************************************************
     *
     * Creates an instance with the given id.
     * 
     * @param  id  the id
     *
     ******************************************************************************************************************/
    public HtmlTemplateContainerView (final @Nonnull String id) 
      {
        super(id);
//        setCaption(typeUri); TODO:for debugging purposes, add everything into a captioned Panel
      }
  }  
