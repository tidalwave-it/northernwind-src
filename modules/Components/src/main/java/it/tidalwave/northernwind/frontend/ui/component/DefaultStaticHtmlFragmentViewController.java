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
package it.tidalwave.northernwind.frontend.ui.component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Map.Entry;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.Cleanup;
import org.stringtemplate.v4.ST;

/***********************************************************************************************************************
 *
 * TODO: refactor with HtmlHolder
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class DefaultStaticHtmlFragmentViewController implements StaticHtmlFragmentViewController
  {
    @Nonnull
    private final StaticHtmlFragmentView view;

    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link StaticHtmlFragmentView} with the given {@link SiteNode}.
     *
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultStaticHtmlFragmentViewController (final @Nonnull StaticHtmlFragmentView view,
                                                    final @Nonnull SiteNode siteNode)
      {
        this.view = view;
      }

    protected void populate (final @Nonnull String htmlResourceName, final @Nonnull Map<String, String> attributes)
      throws IOException
      {
        final Resource htmlResource = new ClassPathResource(htmlResourceName, getClass());
        final @Cleanup Reader r = new InputStreamReader(htmlResource.getInputStream());
        final CharBuffer charBuffer = CharBuffer.allocate((int)htmlResource.contentLength());
        final int length = r.read(charBuffer);
        r.close();
        final String html = new String(charBuffer.array(), 0, length);

        ST template = new ST(html, '$', '$');

        for (final Entry<String, String> entry : attributes.entrySet())
          {
            template = template.add(entry.getKey(), entry.getValue());
          }

        view.setContent(template.render());
      }
  }
