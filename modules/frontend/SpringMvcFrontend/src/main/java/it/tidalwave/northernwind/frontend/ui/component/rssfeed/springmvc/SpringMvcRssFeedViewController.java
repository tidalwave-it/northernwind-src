/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
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
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.rssfeed.springmvc;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.SiteNode;
import it.tidalwave.northernwind.frontend.ui.component.htmltemplate.HtmlHolder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class SpringMvcRssFeedViewController 
  {
    @Nonnull
    private final RssFeedView view;
    
    public SpringMvcRssFeedViewController (final @Nonnull RssFeedView view, final @Nonnull SiteNode siteNode)
      {
        this.view = view;
      }
    
    @PostConstruct
    public void initialize() 
      throws IllegalArgumentException, FeedException
      {
        log.info("initialize()");
        final Channel feed = new Channel("rss_2.0");
        feed.setTitle("feed title");  
        feed.setDescription("feed description");  
        feed.setLink("feed link");  
          
        final List<Item> feedItems = new ArrayList<Item>();  
                  
        for (int i = 0; i < 10; i++) 
          {                             
            final Item item = new Item();  
            item.setTitle("title " + i);  
            item.setAuthor("author " + i);  
            item.setPubDate(new Date());  
              
            final Description description = new Description();  
            description.setType("text/html");  
            description.setValue("description " + 1);  
            item.setDescription(description);  
              
            item.setLink("link " + i);  
            feedItems.add(item);  
          }    
        
        feed.setItems(feedItems);
        

//        if (!StringUtils.hasText(feed.getEncoding())) 
//          {
//            feed.setEncoding("UTF-8");
//          }

        final WireFeedOutput feedOutput = new WireFeedOutput();
        final String s = feedOutput.outputString(feed);
        log.info("RSS FEED {}", s);
        view.setContent(s);      
      }
  } 
