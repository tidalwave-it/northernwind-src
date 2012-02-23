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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.htmltextwithtitle;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import org.stringtemplate.v4.ST;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Content;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteNode;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor @Configurable @Slf4j
public class DefaultHtmlTextWithTitleViewController implements HtmlTextWithTitleViewController
  {
    @Nonnull
    private final HtmlTextWithTitleView view;
    
    @Nonnull
    private final SiteNode siteNode;
    
    @Nonnull
    private final Site site;
    
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
            final ResourceProperties viewProperties = siteNode.getPropertyGroup(view.getId());
            final StringBuilder htmlBuilder = new StringBuilder();
            String titleMarkup = "h2";
            
            String template = "$content$";
            
            try
              {
                final String templateRelativePath = viewProperties.getProperty(PROPERTY_WRAPPER_TEMPLATE_RESOURCE);
                final Content content = site.find(Content).withRelativePath(templateRelativePath).result();
                final ResourceProperties templateProperties = content.getProperties();
                template = templateProperties.getProperty(PROPERTY_TEMPLATE, "$content$");
              }
            catch (NotFoundException e)
              {
                // ok, default template
              }
            
            log.debug(">>>> template: {}", template);
            
            for (final String relativePath : viewProperties.getProperty(PROPERTY_CONTENTS))
              {
                final StringBuilder htmlBuilder2 = new StringBuilder();
                final Content content = site.find(Content).withRelativePath(relativePath).result();
                final ResourceProperties contentProperties = content.getProperties();
                
                try
                  {
                    final String title = contentProperties.getProperty(PROPERTY_TITLE);
                    htmlBuilder2.append(String.format("<%s>%s</%s>\n", titleMarkup, title, titleMarkup));
                  }
                catch (NotFoundException e)
                  {
                    // ok, no title
                  }

                String s = "";
                try
                  {
                    s = contentProperties.getProperty(PROPERTY_FULL_TEXT) + "\n";
                  }
                catch (NotFoundException e)
                  {
                    log.warn("", e);
                    s = e.toString();
                  }
                
                htmlBuilder2.append(s);
                final ST t = new ST(template, '$', '$').add("content", htmlBuilder2.toString());
                htmlBuilder.append(t.render());
                
                titleMarkup = "h3";
              }
            
            view.setText(htmlBuilder.toString());
            
            view.setClassName(viewProperties.getProperty(PROPERTY_CLASS, "nw-" + view.getId()));
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