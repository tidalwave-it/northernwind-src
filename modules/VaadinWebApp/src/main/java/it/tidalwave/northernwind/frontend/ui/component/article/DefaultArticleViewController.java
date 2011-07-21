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
package it.tidalwave.northernwind.frontend.ui.component.article;

import it.tidalwave.northernwind.frontend.model.Content;
import it.tidalwave.northernwind.frontend.model.WebSiteModel;
import it.tidalwave.util.NotFoundException;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import static it.tidalwave.northernwind.frontend.ui.component.article.ArticleView.*;

/***********************************************************************************************************************
 *
 * A default implementation of {@link ArticleViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable(preConstruction=true) @Slf4j
public class DefaultArticleViewController implements ArticleViewController
  {
    @Nonnull @Inject
    private WebSiteModel webSiteModel;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link ArticleView} with the given URI.
     * 
     * @param  articleView  the view to populate
     * @param  relativeUri  the content URI
     *
     ******************************************************************************************************************/
    public DefaultArticleViewController (final @Nonnull ArticleView articleView, final @Nonnull String relativeUri) 
      {
        try
          {
            final Content content = webSiteModel.findContentByUri(relativeUri);
            articleView.setText(content.getProperty(PROP_FULL_TEXT));
          }
        catch (NotFoundException e)
          {
            articleView.setText(e.toString());
            log.error("", e);
          }
        catch (IOException e)
          {
            articleView.setText(e.toString());
            log.error("", e);
          }
      }
  }
