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
package it.tidalwave.northernwind.importer.infoglue;

import java.util.SortedMap;
import java.util.TreeMap;
import it.tidalwave.util.Key;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import static it.tidalwave.northernwind.importer.infoglue.Utilities.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ContentParser extends Parser
  {
    private static final List<String> HTML_PROPERTIES = Arrays.asList("FullText", "Template", "Leadin");
    
    private final String language;
    
    private final String comment;

    private boolean inAttributes = false;
    
    public ContentParser (final @Nonnull String xml, 
                          final @Nonnull DateTime latestModificationTime, 
                          final @Nonnull DateTime publishedDateTime, 
                          final @Nonnull String path, 
                          final @Nonnull String language,
                          final @Nonnull String comment)
      throws XMLStreamException 
      {
        super(xml, path, latestModificationTime, publishedDateTime);
        this.language = language;
        this.comment = comment;
        log.debug("path: {}", path);
      }

    @Override
    protected void processStartElement (final @Nonnull String name, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("attributes".equals(name))
          {
            inAttributes = true;  
          }
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        log.trace("processEndElement({})", name);
        
        if ("attributes".equals(name))
          {
            inAttributes = false;  
          }
        else if (inAttributes)
          {
            if (HTML_PROPERTIES.contains(name))
              {
                String xml = "";           
                
                if (name.toLowerCase().startsWith("template"))
                  {
                    xml = builder.toString();
                  }
                else
                  {
                    // Macros must be expanded _before_ formatting HTML, since JTidy screws up Infoglue macro syntax.
                    // OTOH, JTidy urlencodes the URLs, including our own macros... so we must fix them after the fact.
                    xml = urlDecodeMacros(formatHtml(replaceMacros(builder.toString())));
                  }
                
                // FIXME: for StoppingDown
                if (path.startsWith("//content/document/Blog/"))
                  {
                    final String category = path.replace("//content/document/Blog/", "").replaceAll("/.*", "");
                    final String path2 = "/content/document/Blog/" + category + "/";
                    final SortedMap<Key<?>, Object> properties2 = new TreeMap<Key<?>, Object>();
                    properties2.put(new Key<Object>("category"), category.toLowerCase().replace('+', ' ')); 
                    dumpProperties(properties2, path2, "Properties_en");
                  }
                // END FIXME: for StoppingDown
                
//                if (path.equals("//content/document/Resources/Diary.xml/"))
//                  {
//                    ResourceManager.addCommand(new AddResourceCommand(modifiedDateTime, 
//                                                                      "/structure/Diary/entries_en.xml", 
//                                                                      xml.getBytes("UTF-8"),
//                                                                      comment));
//                  }
//                else if (path.equals("//content/document/Resources/Travels.xml/"))
//                  {
//                    ResourceManager.addCommand(new AddResourceCommand(modifiedDateTime, 
//                                                                      "/structure/Travels/entries_en.xml", 
//                                                                      xml.getBytes("UTF-8"),
//                                                                      comment));
//                  }
//                // END FIXME: for StoppingDown
//                else
                  {
                    ResourceManager.addCommand(new AddResourceCommand(modifiedDateTime, 
                                                                      path + toLower(name) + "_" + language + ".html",
                                                                      xml.getBytes("UTF-8"),
                                                                      comment));
                  }
              }
            else
              {
                properties.put(new Key<Object>(toLower(name)), builder.toString()); 
              }
          }
        else
          {
            log.warn("IGNORING {} {}    ", name, builder);  
          }
      }        

    @Override
    protected void finish()
      throws IOException
      {
//        // FIXME: for StoppingDown
//        if (!path.equals("//content/document/Resources/Diary.xml/") 
//            && !path.equals("//content/document/Resources/Travels.xml/"))
//        // END FIXME: for StoppingDown
          {
            if (path.startsWith("//content/document/Blog"))
              {
                String title = (String)properties.get(new Key<String>("title"));
                title = title.replaceAll(" ", "-").replaceAll("[^\\w-]*", "").toLowerCase(); 
                properties.put(new Key<String>("exposedUri"), title);  
              }
                
            dumpProperties("Properties_" + language);
          }
      }
  }
