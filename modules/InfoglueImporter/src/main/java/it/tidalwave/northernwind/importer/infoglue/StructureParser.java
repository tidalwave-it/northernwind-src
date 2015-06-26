/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 * 
 * $Id$
 * 
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.importer.infoglue;

import it.tidalwave.util.Key;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class StructureParser extends Parser
  {
    public static final List<String> IGNORED_PROPERTIES = Arrays.asList("MetaInfo", "baseURL", "dataSource", "xMLData");

    private final String language;

    public StructureParser (String xml,
                            final @Nonnull DateTime modifiedDateTime,
                            final @Nonnull DateTime publishingDateTime,
                            String path,
                            String language)
      throws FileNotFoundException, XMLStreamException
      {
        super(xml, path, modifiedDateTime, publishingDateTime);
        this.language = language;
      }

    @Override
    protected void processEndElement (final @Nonnull String name)
      throws Exception
      {
        log.trace("processEndElement({})", name);

        final String s = builder.toString();

        if ("ComponentStructure".equals(name))
          {
            if (!"".equals(s.trim()))
              {
                try
                  {
                    new LayoutConverter(s, modifiedDateTime, path + "Components_" + language + ".xml", properties).process();
                  }
                catch (Exception e)
                  {
                    log.error("ERROR ON " + builder, e);
                  }
              }
          }
        else if (!Arrays.asList("attributes", "article").contains(name))
          {
            if (!s.equals("_Standard Pages"))
              {
                String propertyName = name;
                String propertyValue = s;

                if ("NiceURIName".equals(propertyName))
                  {
                    propertyName = "exposedUri";
                    propertyValue = propertyValue.replace("Blog___News", "Blog").toLowerCase().replaceAll("_", "-");
                  }

                else if ("NavigationTitle".equals(propertyName))
                  {
                    propertyName = "navigationLabel";
                  }

                if (!IGNORED_PROPERTIES.contains(propertyName))
                  {
                    properties.put(new Key<Object>(toLower(propertyName)), propertyValue);
                  }
              }
          }
      }

    @Override
    protected void finish()
      throws IOException
      {
        log.debug("properties: " + properties);
        final Key<Object> PROPERTY_EXPOSED_URI = new Key<Object>("exposedUri");
        final Key<Object> PROPERTY_NAVIGATION_LABEL = new Key<Object>("navigationLabel");

        // FIXME: for StoppingDown
        if (path.equals("//structure/RSS+Feeds/Override"))
          {
            properties.put(PROPERTY_EXPOSED_URI, "feeds");
          }
        if (path.equals("//structure/RSS+Feeds/Blog+RSS+Feed/Override"))
          {
            properties.put(PROPERTY_EXPOSED_URI, "blog.rss");
          }
        if (path.equals("//structure/RSS+Feeds/News+RSS+Feed/Override"))
          {
            properties.put(PROPERTY_EXPOSED_URI, "news.rss");
          }
        if (path.startsWith("//structure/Blog/") && !path.equals("//structure/Blog/"))
          {
            properties.clear();
            final String category = path.replace("//structure/Blog/", "").replace('+', ' ').replaceAll("/$", "");
            properties.put(new Key<String>("placeHolder"), "true");
            properties.put(new Key<String>("navigationLabel"), category);
            properties.put(new Key<String>("exposedUri"), category.toLowerCase());
          }

        // END FIXME: for StoppingDown

        if (!properties.containsKey(PROPERTY_EXPOSED_URI) && properties.containsKey(PROPERTY_NAVIGATION_LABEL))
          {
            properties.put(PROPERTY_EXPOSED_URI, properties.get(PROPERTY_NAVIGATION_LABEL).toString().toLowerCase());
          }

        dumpProperties("Properties_" + language);
      }
  }
