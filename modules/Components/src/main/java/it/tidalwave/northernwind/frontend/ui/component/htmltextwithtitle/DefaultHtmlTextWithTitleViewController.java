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
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlTextWithTitleViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @Inject @Nonnull
    private Site site;
    
    private final HtmlTextWithTitleView view;
    
    private final SiteNode siteNode;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link HtmlTextWithTitleView} with the given URI.
     * 
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultHtmlTextWithTitleViewController (final @Nonnull HtmlTextWithTitleView view, 
                                                   final @Nonnull SiteNode siteNode) 
      {
        this.view = view;
        this.siteNode = siteNode;
      }
    
    /*******************************************************************************************************************
     *
     * Initializes this controller.
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void initialize()
      {
        try
          {
            final StringBuilder htmlBuilder = new StringBuilder();
            
            for (final String relativePath   : siteNode.getProperties(view.getId()).getProperty(PROPERTY_CONTENTS))
              {
                final Content content = site.find(Content).withRelativePath(relativePath).result();
                htmlBuilder.append(content.getProperties().getProperty(PROPERTY_FULL_TEXT)).append("\n");
              }
            
            view.setText(htmlBuilder.toString());
          }
        catch (NotFoundException e)
          {
            view.setText(e.toString());
            log.error("", e.toString());
          }
        catch (IOException e)
          {
            view.setText(e.toString());
            log.error("", e);
          }
      }
  }