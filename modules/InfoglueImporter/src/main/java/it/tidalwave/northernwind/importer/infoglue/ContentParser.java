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
import java.io.IOException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ContentParser extends Parser
  {
    private final String language;

    private boolean inAttributes = false;

    public ContentParser (final @Nonnull String xml, final @Nonnull DateTime dateTime, final @Nonnull String path, final @Nonnull String language) 
      {
        super(xml, path, dateTime);
        this.language = language;
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
        log.debug("processEndElement({})", name);
        
        if ("attributes".equals(name))
          {
            inAttributes = false;  
          }
        else if (inAttributes)
          {
            if (Arrays.asList("FullText", "Template", "Leadin").contains(name))
              {
                // FIXME: format HTML
                ResourceManager.addResource(new Resource(dateTime, path + name + "_" + language + ".html", builder.toString().getBytes("UTF-8")));
//                    addResource(new Resource(dateTime, path + name + ".html", dumpXml("<body>" + builder.toString() + "</body>")));
              }
            else
              {
                properties.put(new Key<Object>(name), builder.toString()); 
              }
          }
      }        

    @Override
    protected void finish()
      throws IOException
      {
        dumpProperties("Properties_" + language);
      }
  }

