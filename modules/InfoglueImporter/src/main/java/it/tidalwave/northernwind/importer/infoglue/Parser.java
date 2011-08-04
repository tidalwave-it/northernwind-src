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

import it.tidalwave.northernwind.core.impl.model.DefaultResourceProperties;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.StringReader;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
    @Nonnull
    private final String contents;

    protected final StringBuilder builder = new StringBuilder();
    private final String spaces = "                                                                ";
    protected int indent;
    protected final SortedMap<Key<?>, Object> properties = new TreeMap<Key<?>, Object>();
    protected final String path;
    protected final DateTime dateTime;        

    public Parser (final @Nonnull String contents, final @Nonnull String path, final @Nonnull DateTime dateTime) 
      {
        log.debug("Parsing {} ...", contents);
        this.contents = contents;
        this.path = path;
        this.dateTime = dateTime;
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
                  log.debug("END DOCUMENT");
                  finish();
                  break;

                case XMLEvent.ATTRIBUTE:
                  log.debug("ATTRIBUTE     {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  processAttribute(reader.getName().getLocalPart(), reader);
                  break;

                case XMLEvent.START_ELEMENT:
                  log.debug("START ELEMENT {} {}", eventType, reader.getName());
                  builder.delete(0, builder.length());
                  processStartElement(reader.getName().getLocalPart(), reader);
                  indent++;
                  break;

                case XMLEvent.END_ELEMENT:
                  log.debug("END ELEMENT   {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  indent--;
                  processEndElement(reader.getName().getLocalPart());
                  break;

                default:
                  log.debug("DEFAULT       {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
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
        final DefaultResourceProperties rp = new DefaultResourceProperties(new Id(""), properties, null);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rp.as(Marshallable).marshal(baos);
        baos.close();
        ResourceManager.addResource(new Resource(dateTime, path + fileName + ".xml", baos.toByteArray()));
      }
  }
