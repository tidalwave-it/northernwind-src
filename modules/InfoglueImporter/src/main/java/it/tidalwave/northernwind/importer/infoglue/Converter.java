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
package it.tidalwave.northernwind.importer.infoglue;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public abstract class Converter 
  {
    private final BASE64Decoder decoder = new BASE64Decoder();

    protected final StringBuilder builder = new StringBuilder();
    
    protected int indent;
    
    protected int localLevel;
    
    protected final XMLStreamReader reader;
    
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendYear(4, 4)
                                                                                     .appendLiteral("-")
                                                                                     .appendMonthOfYear(2)
                                                                                     .appendLiteral("-")
                                                                                     .appendDayOfMonth(2)
                                                                                     .appendLiteral("T")
                                                                                     .appendHourOfDay(2)
                                                                                     .appendLiteral(":")
                                                                                     .appendMinuteOfHour(2)
                                                                                     .appendLiteral(":")
                                                                                     .appendSecondOfMinute(2)
                                                                                     .appendLiteral(".")
                                                                                     .appendMillisOfSecond(3)
                                                                                     .appendTimeZoneOffset("", true, 2, 2)
                                                                                     .toFormatter();
    
    public Converter (final @Nonnull String contents) 
      throws XMLStreamException 
      {
        log.debug("Parsing {}", contents);
        reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(contents));
      }
    
    public Converter (final @Nonnull InputStream is) 
      throws XMLStreamException 
      {
        log.debug("Parsing {}", is);
        reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
      }
    
    public Converter (final @Nonnull Converter parent)
      {
        this.indent = parent.indent;
        this.reader = parent.reader;
      }
    
    public void process() 
      throws Exception
      {
        log.trace("process() - {}", this);
        start();
        
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
                  log.trace("START ELEMENT {} {} ({})", new Object[] { eventType, reader.getName(), localLevel });
                  builder.delete(0, builder.length());
                  processStartElement(reader.getName().getLocalPart(), reader);
                  indent++;
                  localLevel++;
                  break;

                case XMLEvent.END_ELEMENT:
                  log.trace("END ELEMENT   {} {} ({}): {}", new Object[] { eventType, reader.getName(), localLevel, builder.substring(0, Math.min(1000, builder.length())) });
                  --indent;
                  
                  processEndElement(reader.getName().getLocalPart());
                  
                  if (--localLevel < 0)
                    {
                      finish();
                      log.info("Finished this level {} - {}", reader.getName(), this);
                      return;
                    }
//                  if (--localLevel < 0)
//                    {
//                      finish();
//                      log.info("Finished this level");
//                      return;
//                    }
//                  else
//                    {
//                      processEndElement(reader.getName().getLocalPart());
//                    } 
                  // FIXME: should reset the buffer
                  break;

                default:
                  log.trace("DEFAULT       {} {}: {}", new Object[] { eventType, reader.getName(), builder.substring(0, Math.min(1000, builder.length())) });
                  break;
              }
          }
      }

    protected void start()
      throws Exception
      {           
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
    
    @Nonnull
    protected String contentAsString()
      {
        return builder.toString();  
      }
    
    @Nonnull
    protected int contentAsInteger()
      {
        return Integer.parseInt(builder.toString());  
      }
    
    @Nonnull
    protected boolean contentAsBoolean()
      {
        return Boolean.parseBoolean(builder.toString());  
      }
    
    @Nonnull
    protected DateTime contentAsDateTime()
      {
        return FORMATTER.parseDateTime(builder.toString());  
      }
    
    @Nonnull
    protected byte[] contentAsBytes()
      throws IOException
      {
        return decoder.decodeBuffer(builder.toString());
      }
  }