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
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.Site;
import it.tidalwave.northernwind.frontend.model.SiteNode;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.model.Content.Content;
import static it.tidalwave.northernwind.frontend.ui.component.Properties.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link HtmlTextWithTitleViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id: DefaultHtmlTextWithTitleViewController.java,v 2b1a28213ed2 2011/07/25 23:34:35 fabrizio $
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
            final Key<String> PROP_CONTENT = new Key<String>(viewId + ".content"); // FIXME: have a subproperty group with the name
            final String contentUri = siteNode.getProperty(PROP_CONTENT);
            
            // FIXME: should be fixed in the Infoglue importer
            final String fixedContentUri = r("/" + contentUri.replaceAll("Mobile", ""));
            
            final Content content = site.find(Content).withRelativeUri(fixedContentUri).result();
            view.setText(content.getProperty(PROPERTY_FULL_TEXT));
          }
        catch (NotFoundException e)
          {
            view.setText(e.toString());
            log.error("", e);
          }
        catch (IOException e)
          {
            view.setText(e.toString());
            log.error("", e);
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String r (final @Nonnull String s)
      {
        return "".equals(s) ? "/" : s;  
      }
  }