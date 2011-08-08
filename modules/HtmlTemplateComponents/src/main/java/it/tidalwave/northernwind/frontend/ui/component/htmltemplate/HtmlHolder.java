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
package it.tidalwave.northernwind.frontend.ui.component.htmltemplate;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.stringtemplate.v4.ST;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.Site;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.frontend.ui.SiteView.*;

/***********************************************************************************************************************
 *
 * The HtmlTemplate specialization of {@link SiteViewController}.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j
public class HtmlHolder 
  {
    private final Map<String, String> attributeMap = new HashMap<String, String>();
            
    private final List<HtmlHolder> contents = new ArrayList<HtmlHolder>();
    
    private String template;
    
    @Inject
    private Site site;
    
    /*******************************************************************************************************************
     *
     * Creates an instance with the given name.
     * 
     * @param  name  the component name
     *
     ******************************************************************************************************************/
    public HtmlHolder (final @Nonnull Id id) 
      {
        attributeMap.put("style", NW + id.stringValue());
        attributeMap.put("content", "");
        
        try
          {
            loadTemplate();      
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);  
          }
      }
    
    public HtmlHolder (final @Nonnull String html) 
      {
        addAttribute("content", html);
        template = "$content$";
      }
    
//    @Override
    public void setContent (final @Nonnull String content) 
      {
        addAttribute("content", content);
//        setValue(html);
      }    
    
    public void addComponent (final @Nonnull HtmlHolder child) 
      {
        contents.add(child);
      }
    
    public void addAttribute (final @Nonnull String name, final @Nonnull String value)
      {
        attributeMap.put(name, value);  
      }
    
    @Nonnull
    public String asString()
      throws IOException
      {
        ST t = new ST(template, '$', '$');

        for (final Entry<String, String> entry : attributeMap.entrySet())
          {
            t = t.add(entry.getKey(), entry.getValue());  
          }

        final StringBuilder builder = new StringBuilder();

        for (final HtmlHolder child : contents)
          {
            builder.append(child.asString()).append("\n");  
          }

        t = t.add("content", builder.toString());
        t = t.add("contextPath", site.getContextPath());

        return t.render();
      }
    
    private void loadTemplate()
      throws IOException
      { 
        // FIXME: this should be done only once...
        final String templateName = getClass().getSimpleName() + ".html";
        
        try
          {
            final Resource htmlResource = new ClassPathResource(templateName, getClass());  
            final @Cleanup Reader r = new InputStreamReader(htmlResource.getInputStream());
            final CharBuffer charBuffer = CharBuffer.allocate((int)htmlResource.contentLength());
            final int length = r.read(charBuffer);
            r.close();
            template = new String(charBuffer.array(), 0, length);
          }
        catch (FileNotFoundException e) // no specific template, fallback
          {
            log.info("No template for {}, using default", templateName);
            template = "<div class='$style$'>\n$content$\n</div>\n";
          }
      }  
  }
