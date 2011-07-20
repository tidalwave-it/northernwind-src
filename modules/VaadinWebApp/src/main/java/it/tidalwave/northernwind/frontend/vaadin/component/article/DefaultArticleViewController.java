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
package it.tidalwave.northernwind.frontend.vaadin.component.article;

import it.tidalwave.northernwind.frontend.vaadin.Content;
import it.tidalwave.northernwind.frontend.vaadin.WebSiteModel;
import java.io.IOException;
import javax.annotation.Nonnull;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultArticleViewController implements ArticleViewController
  {
    public DefaultArticleViewController (final @Nonnull WebSiteModel webSiteModel,
                                         final @Nonnull ArticleView articleView, 
                                         final @Nonnull String uri) 
      {
        try
          {
            final Content content = webSiteModel.getContent(uri);
            articleView.setText(content.get("FullText_en.html", String.class));
          }
        catch (IOException e)
          {
            articleView.setText(e.toString());
          }
      }
  }
