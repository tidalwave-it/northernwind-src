package it.tidalwave.northernwind.infoglueexporter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Stack;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import sun.misc.BASE64Decoder;

/**
 * TODO
 *
 * layout_en.xml contiene la struttura dei bean (e.g. Vaadin) della pagina. E' implementato con Spring e XmlBeanContext. Viene istanziato per sessione. Può contenere proprietà. E' ereditario. Particolarmente comoda
 * la struttura nidificata di Spring, ma può anche usare ref se uno preferisce.
 * layour-override_en.xml è simile, ma fa override e non è ereditato.
 * properties_en.xml contiene le proprietà ed è ereditato. Una proprietà può essere p.es. una label o il titolo della pagina o la risorsa con cui popolare il bean.
 * properties-override_en.xml fa override delle proprietà e non è ereditato.
 *
 */
public class Main 
  {
    public static final File hgFolder = new File("/home/fritz/.nw/test/root");      
    
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
        process("/home/fritz/Downloads/Export__blueBill_2011-07-17_1747.xml");
        ResourceManager.addAndCommitResources();
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
        DateTime dateTime = null;
        
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
                      System.err.printf("%spath: %s\n", spaces.substring(0, indent * 2), path);
                    }
                  
                  if ("languageCode".equals(name))
                    {
                      languageCode = builder.toString();  
                    }
                  
                  if ("modifiedDateTime".equals(name))
                    {
                      dateTime = FORMATTER.parseDateTime(builder.toString());  
                        System.err.println("date " + builder + " parsed as " + dateTime);
                    }
                  
                  if ("assetFileName".equals(name))
                    {
                      assetFileName = builder.toString();  
                    }
                  
                  // TODO: we're not tracking document deletion
                  // TODO: when a document was added, it wasn't necessarily immediately published - put those documents in branches, merged when they are published
                  
                  if ("escapedVersionValue".equals(name))
                    {
                      String fixedPath = path.replaceAll("/$", "") + "/";
                      System.err.println("Processing " + fixedPath);
                      fixedPath = fixedPath.replaceAll("^/blueBill/", "");
                      
                      if (fixedPath.startsWith("Mobile"))
                        {
                          fixedPath = fixedPath.replaceAll("^Mobile/", "content/document/");
                          final String xml = builder.toString().replace("cdataEnd", "]]>");
                          new ContentParser(xml, dateTime, fixedPath, languageCode).process();
                        }
                      
                      else if (fixedPath.startsWith("Meta+info+folder/blueBill/Mobile"))
                        {
                          fixedPath = fixedPath.replaceAll("^Meta\\+info\\+folder/blueBill/Mobile/", "structure/")
                                               .replaceAll("_Standard\\+Pages/", "/")
                                               .replaceAll("\\+Metainfo/", "/");
                          fixedPath = fixedPath.replaceAll("_Standard\\+Pages/$", "layout");
                          fixedPath = fixedPath.replaceAll("Meta\\+info\\+folder/blueBill/Mobile/", "structure/Override");
//                          fixedPath = fixedPath.replaceAll("(^.*/)$", "$1layout_");
                          final String xml = builder.toString().replace("cdataEnd", "]]>");
                          new StructureParser(xml, dateTime, fixedPath, languageCode).process(); 
                        }
                    }
                  
                  if ("assetBytes".equals(name))
                    {
                      String fixedPath = "content/media/" + assetFileName;
                      System.err.println("Processing " + fixedPath);
                      // FIXME: find the timestamp
                      ResourceManager.addMedia(new Resource(new DateTime(), fixedPath, decoder.decodeBuffer(builder.toString())));
                    }
                  
                  indent--;
                    
                default:
                  name = event.getName().toString();
                  System.err.printf("%s%d %s: %s\n",  spaces.substring(0, indent * 2), eventType, name, builder.substring(0, Math.min(1000, builder.length())));
                  break;
              }
          }
      }    
  }
