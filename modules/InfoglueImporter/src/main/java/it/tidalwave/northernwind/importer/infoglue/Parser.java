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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.joda.time.DateTime;
import it.tidalwave.northernwind.core.impl.model.DefaultResourceProperties;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Marshallable.Marshallable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class Parser
  {
    private static final Map<String, DateTime> creationTimeByPath = new HashMap<String, DateTime>();
    private static final Map<String, DateTime> publishingTimeByPath = new HashMap<String, DateTime>();
    
    @Nonnull
    private final String contents;

    protected final StringBuilder builder = new StringBuilder();
    protected int indent;
    protected final SortedMap<Key<?>, Object> properties = new TreeMap<Key<?>, Object>();
    protected final String path;
    protected final DateTime modifiedDateTime;        
    private final DateTime publishDateTime;

    public Parser (final @Nonnull String contents, 
                   final @Nonnull String path, 
                   final @Nonnull DateTime modifiedDateTime,
                   final @Nonnull DateTime publishedDateTime) 
      {
        log.debug("Parsing {} ...", contents);
        this.contents = contents;
        this.path = path;
        this.modifiedDateTime = modifiedDateTime;
        this.publishDateTime = publishedDateTime;
      }

    public void process() 
      throws Exception
      {
        log.info("process()");
        final XMLInputFactory f = XMLInputFactory.newInstance();
        final XMLStreamReader reader = f.createXMLStreamReader(new StringReader(contents));

        while (reader.hasNext()) 
          {
            reader.next();
            final int eventType = reader.getEventType();

            switch (eventType)
              {
                case XMLEvent.CHARACTERS:
                  builder.append(reader.getText());
                  break;

                case XMLEvent.CDATA:
                  throw new RuntimeException("CDATA!");

                case XMLEvent.END_DOCUMENT:
                  log.trace("END DOCUMENT");
                  finish();
                  break;

                case XMLEvent.ATTRIBUTE:
                  log.trace("ATTRIBUTE     {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  processAttribute(reader.getName().getLocalPart(), reader);
                  break;

                case XMLEvent.START_ELEMENT:
                  log.trace("START ELEMENT {} {}", eventType, reader.getName());
                  builder.delete(0, builder.length());
                  processStartElement(reader.getName().getLocalPart(), reader);
                  indent++;
                  break;

                case XMLEvent.END_ELEMENT:
                  log.trace("END ELEMENT   {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  indent--;
                  processEndElement(reader.getName().getLocalPart());
                  break;

                default:
                  log.trace("DEFAULT       {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  break;
              }
          }
      }

    protected void processAttribute (final @Nonnull String name, final @Nonnull XMLStreamReader reader)
      throws Exception
      {           
      }

    protected void processStartElement (final @Nonnull String name, final @Nonnull XMLStreamReader reader)
      throws Exception
      {           
      }

    protected abstract void processEndElement (@Nonnull String name)
      throws Exception;

    protected void finish()
      throws Exception
      {           
      }

    protected void dumpProperties (final @Nonnull String fileName)
      throws IOException
      {
        DateTime creationTime = creationTimeByPath.get(path);
        
        if (creationTime == null)
          {
            creationTime = modifiedDateTime;
            creationTimeByPath.put(path, creationTime);
          }

        if (publishDateTime != null)
          {
            publishingTimeByPath.put(path, publishDateTime);
          }
        
        final DateTime pdt = publishingTimeByPath.get(path);

        if (pdt != null)
          {
            properties.put(new Key<Object>("publishingDateTime"), pdt);  
          }
        
        properties.put(new Key<Object>("creationDateTime"), creationTime);
        properties.put(new Key<Object>("latestModificationDateTime"), modifiedDateTime);
        final DefaultResourceProperties rp = new DefaultResourceProperties(new Id(""), properties, null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rp.as(Marshallable).marshal(baos);
        baos.close();
        ResourceManager.addResource(new Resource(modifiedDateTime, path + fileName + ".xml", baos.toByteArray()));
      }
    
    @Nonnull
    public static String toLower (final @Nonnull String string)
      {
        return "".equals(string) ? "" : string.substring(0, 1).toLowerCase() + string.substring(1);  
      }
  }
