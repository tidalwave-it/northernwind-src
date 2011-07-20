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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.annotation.Nonnull;
import lombok.Cleanup;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultArticleViewController implements ArticleViewController
  {
    private final Content resource;
    
    public DefaultArticleViewController (final @Nonnull ArticleView articleView, final @Nonnull Content resource) 
      {
        this.resource = resource;
        final File file = new File(resource.getPath(), "FullText_en.html");
        
        try
          {
            @Cleanup final FileReader fr = new FileReader(file);
            final StringBuilder builder = new StringBuilder();
            final char[] chars = new char[(int)file.length()];
            fr.read(chars);
            articleView.setText(new String(chars));
          }
        catch (IOException e)
          {
            articleView.setText(e.toString());
          }
      }
  }
