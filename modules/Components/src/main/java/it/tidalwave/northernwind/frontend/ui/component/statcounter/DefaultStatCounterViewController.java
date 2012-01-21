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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.statcounter;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.DefaultStaticHtmlFragmentViewController;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable
public class DefaultStatCounterViewController extends DefaultStaticHtmlFragmentViewController implements StatCounterViewController
  {  
    @Nonnull
    private final StatCounterView view;
    
    @Nonnull
    private final SiteNode siteNode;
    
    /*******************************************************************************************************************
     *
     * Creates an instance for populating the given {@link StatCounterView} with the given {@link SiteNode}.
     * 
     * @param  view              the related view
     * @param  siteNode          the related {@link SiteNode}
     *
     ******************************************************************************************************************/
    public DefaultStatCounterViewController (final @Nonnull StatCounterView view, 
                                             final @Nonnull SiteNode siteNode) 
      throws IOException 
      {
        super(view, siteNode);
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
      throws IOException, NotFoundException
      {
        final ResourceProperties properties = siteNode.getPropertyGroup(view.getId());
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("project", properties.getProperty(PROPERTY_PROJECT, "")); 
        attributes.put("security", properties.getProperty(PROPERTY_SECURITY, "")); 
        attributes.put("invisible", Boolean.parseBoolean(properties.getProperty(PROPERTY_INVISIBLE, "true")) ? "1" : "0"); 

        final String message = properties.getProperty(PROPERTY_MESSAGE, "{0}");
        String messagePrefix = message.replaceAll("\\{0\\}.*", "");
        String messagePostfix = message.replaceAll(".*\\{0\\}", "");
        attributes.put("messagePrefix", messagePrefix); 
        attributes.put("messagePostfix", messagePostfix); 
        
        final StringBuilder builder = new StringBuilder();
        try
          {
            builder.append(String.format("var sc_partition = %s;\n", properties.getProperty(PROPERTY_PARTITION)));
          }
        catch (NotFoundException e)
          {
          }
        try
          {
            builder.append(String.format("var sc_click_stat = %s;\n", properties.getProperty(PROPERTY_CLICK_STAT)));
          }
        catch (NotFoundException e)
          {
          }
        try
          {
            builder.append(String.format("var sc_text = %s;\n", properties.getProperty(PROPERTY_TEXT)));
          }
        catch (NotFoundException e)
          {
          }
        
        attributes.put("otherParameters", builder.toString()); 
        
        populate("StatCounterHtmlFragment.txt", attributes);
      }
  }
