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
package it.tidalwave.northernwind.importer.infoglue;

import it.tidalwave.util.Key;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
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
    private final String language;

    public StructureParser (String xml, 
                            final @Nonnull DateTime modifiedDateTime, 
                            final @Nonnull DateTime publishingDateTime, 
                            String path, 
                            String language) 
      throws FileNotFoundException 
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
                
                if (!"MetaInfo".equals(propertyName))
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
        dumpProperties("Properties_" + language);
      }
  }
