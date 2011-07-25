/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tidalwave.northernwind.infoglueexporter;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import org.joda.time.DateTime;

/**
 *
 * @author fritz
 */
public abstract class Parser
  {
    @Nonnull
    private final String contents;

    protected final StringBuilder builder = new StringBuilder();
    private final String spaces = "                                                                ";
    protected int indent;
    protected final SortedMap<String, String> properties = new TreeMap<String, String>();
    protected final String path;
    protected final DateTime dateTime;        

    public Parser (final @Nonnull String contents, final @Nonnull String path, final @Nonnull DateTime dateTime) 
      {
        this.contents = contents;
        this.path = path;
        this.dateTime = dateTime;
      }

    public void process () 
      throws Exception
      {
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
                      finish();
                      break;
                        
                    case XMLEvent.ATTRIBUTE:
                      log("%d %s: %s", eventType, reader.getName()  , builder.substring(0, Math.min(1000, builder.length())));
                      processAttribute(reader.getName().getLocalPart(), reader);
                      break;

                    case XMLEvent.START_ELEMENT:
                      log("%d %s: %s", eventType, reader.getName()  , builder.substring(0, Math.min(1000, builder.length())));
                      builder.delete(0, builder.length());
                      processStartElement(reader.getName().getLocalPart(), reader);
                      indent++;
                      break;

                    case XMLEvent.END_ELEMENT:
                      indent--;
                      processEndElement(reader.getName().getLocalPart());

                    default:
                      final QName name = reader.getName();
                      log("%d %s: %s", eventType, name, builder.substring(0, Math.min(1000, builder.length())));
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
        
        protected void dumpPropertiesAsResourceBundle (final @Nonnull String fileName)
          throws UnsupportedEncodingException
          {
            final StringBuilder builder = new StringBuilder();
            
            for (final Entry<String, String> entry : properties.entrySet())
              {
                builder.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");  
              }
            
            ResourceManager.addResource(new Resource(dateTime, path + fileName, builder.toString().getBytes("UTF-8")));
          }
        
        protected void log (final @Nonnull String format, final @Nonnull Object ... args)
          {
            System.err.printf(spaces.substring(0, indent * 2) + format + "\n", args);
          }
      }
    
