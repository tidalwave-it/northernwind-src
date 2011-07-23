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

import javax.annotation.Nonnull;
import it.tidalwave.util.Key;

/***********************************************************************************************************************
 *
 * An {@code ArticleView} is a simple text with an optional title.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ArticleView // FIXME: rename to CaptionedText?
  {
    public static final Key<String> PROP_FULL_TEXT = new Key<String>("FullText_en.html");
    
    /*******************************************************************************************************************
     *
     * Sets the text content.
     * 
     * @param  text  the text
     *
     ******************************************************************************************************************/
    public void setText (@Nonnull String string);
  }
