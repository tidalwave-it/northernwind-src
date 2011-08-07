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

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class Main 
  {
    private static ApplicationContext applicationContext;
        
    public static final File hgFolder = new File("target/root");      
    
    /* package */ static final Map<String, String> assetFileNameMapByKey = new HashMap<String, String>();
    
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
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void main (String[] args)
      throws Exception
      {
        applicationContext = new ClassPathXmlApplicationContext(
                "classpath*:/META-INF/*AutoBeans.xml",
                "classpath*:/META-INF/StandAloneConfigurationBeans.xml",
                "classpath*:/META-INF/SimpleLocalFileSystemBeans.xml");
        process(System.getProperty("user.home") + "/Downloads/Export__blueBill_2011-07-17_1747.xml");
        ResourceManager.addAndCommitResources();
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg tag converted");
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void process (final @Nonnull String string)
      throws Exception
      {
        final BASE64Decoder decoder = new BASE64Decoder();

        final XMLInputFactory f = XMLInputFactory.newInstance();
        final XMLStreamReader event = f.createXMLStreamReader(new FileInputStream(string));
        final StringBuilder builder = new StringBuilder();
        int indent = 0;
        final Stack<String> paths = new Stack<String>();
        String path = "/";
        final String spaces = "                                                                ";
        String languageCode = "";
        String assetFileName = "";
        String assetKey = "";
        DateTime modifiedDateTime = null;
        DateTime publishDateTime = null;
        
        hgFolder.mkdirs();
        Utilities.exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg init");
        
        while (event.hasNext()) 
          {
            event.next();
            final int eventType = event.getEventType();
            
            switch (eventType)
              {
                case XMLEvent.CHARACTERS:
                  builder.append(event.getText());
                  break;
                    
                case XMLEvent.CDATA:
                  throw new RuntimeException("CDATA!");
                    
                case XMLEvent.END_DOCUMENT:
                  break;
                    
                case XMLEvent.START_ELEMENT:
                  paths.push(path);
                  builder.delete(0, builder.length());
                  indent++;
                  break;
                    
                case XMLEvent.END_ELEMENT:
                  path = paths.pop();
                  String name = event.getName().toString();
                  
                  if ("name".equals(name))
                    {
                      path += URLEncoder.encode(builder.toString(), "UTF-8") + "/";
                      log.trace("{}path: {}", spaces.substring(0, indent * 2), path);
                    }
                  
                  else if ("languageCode".equals(name))
                    {
                      languageCode = builder.toString();  
                    }
                  
                  else if ("modifiedDateTime".equals(name))
                    {
                      modifiedDateTime = FORMATTER.parseDateTime(builder.toString());  
                      log.trace("modifiedDateTime {} parsed as {}", builder, modifiedDateTime);
                    }
                  
                  else if ("publishDateTime".equals(name))
                    {
                      publishDateTime = FORMATTER.parseDateTime(builder.toString());  
                      log.trace("publishDateTime {} parsed as {}", builder, publishDateTime);
                    }
                  
                  else if ("assetFileName".equals(name))
                    {
                      assetFileName = builder.toString();  
                    }
                  
                  else if ("assetKey".equals(name))
                    {
                      assetKey = builder.toString();  
                      assetFileNameMapByKey.put(assetKey, assetFileName);
                    }
                  
                  // TODO: we're not tracking document deletion
                  // TODO: when a document was added, it wasn't necessarily immediately published - put those documents in branches, merged when they are published
                  
                  else if ("escapedVersionValue".equals(name))
                    {
                      String fixedPath = path.replaceAll("/$", "") + "/";
                      log.info("Processing {} ...", fixedPath);
                      
                      if (fixedPath.equals("/blueBill/License/"))
                        {
                          fixedPath = "/blueBill/Mobile/License/";   
                        }
                      
                      else if (fixedPath.equals("/blueBill/Mobile/Contact/"))
                        {
                          fixedPath = "/blueBill/Mobile/Contacts/";   
                        }
                      
                      else if (fixedPath.equals("/blueBill/Meta+info+folder/blueBill/_Standard+Pages/Contacts+Metainfo/"))
                        {
                          fixedPath = "/blueBill/Meta+info+folder/blueBill/Mobile/_Standard+Pages/Contacts+Metainfo/";   
                        }
                      
                      fixedPath = fixedPath.replaceAll("^/blueBill/", "");
                      final String content = builder.toString().replace("cdataEnd", "]]>");
                      
                      if (fixedPath.startsWith("Mobile"))
                        {
                          fixedPath = fixedPath.replaceAll("^Mobile", "content/document");
                          log.info("PBD " + publishDateTime + " " + fixedPath);
                          new ContentParser(content, modifiedDateTime, publishDateTime, fixedPath, languageCode).process();
                        }
                      
                      else if (fixedPath.startsWith("Meta+info+folder/blueBill/Mobile"))
                        {
                          fixedPath = fixedPath.replaceAll("^Meta\\+info\\+folder/blueBill/Mobile", "structure")
                                               .replaceAll("_Standard\\+Pages/", "/")
                                               .replaceAll("\\+Metainfo/", "/");
                          fixedPath = fixedPath.replaceAll("_Standard\\+Pages/$", "Override");
                          fixedPath = fixedPath.replaceAll("Meta\\+info\\+folder/blueBill/Mobile/", "structure/Override");
//                          fixedPath = fixedPath.replaceAll("(^.*/)$", "$1layout_");
                          new StructureParser(content, modifiedDateTime, publishDateTime, fixedPath, languageCode).process(); 
                        }

                      publishDateTime = null;
                    }
                  
                  else if ("assetBytes".equals(name))
                    {
                      String fixedPath = "content/media/" + assetFileName;
                      log.info("Processing {} ...", fixedPath);
                      // FIXME: find the timestamp
                      ResourceManager.addMedia(new Resource(new DateTime(), fixedPath, decoder.decodeBuffer(builder.toString())));
                    }
                  
                  indent--;
                    
                default:
                  name = event.getName().toString();
                  log.trace("{}{} {}: {}", new Object[]
                    {
                      spaces.substring(0, indent * 2), eventType, name, builder.substring(0, Math.min(1000, builder.length()))
                    });
                  break;
              }
          }
      }    
  }
