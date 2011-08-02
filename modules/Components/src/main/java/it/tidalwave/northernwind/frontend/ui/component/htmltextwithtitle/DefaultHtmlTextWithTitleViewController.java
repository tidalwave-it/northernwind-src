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
@Configurable(preConstruction=true) @Slf4j
public class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @Nonnull @Inject
    private Site site;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link HtmlTextWithTitleView} with the given URI.
     * 
     * @param  view              the related view
     * @param  viewId            the id of the view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultHtmlTextWithTitleViewController (final @Nonnull HtmlTextWithTitleView view, 
                                                   final @Nonnull Id viewId,
                                                   final @Nonnull SiteNode siteNode) 
      {
        try
          {
            final String contentUri = siteNode.getProperties(viewId).getProperty(PROPERTY_CONTENTS);
            final Content content = site.find(Content).withRelativeUri(contentUri).result(); // FIXME: it's a multi-value property
            view.setText(content.getProperties().getProperty(PROPERTY_FULL_TEXT));
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