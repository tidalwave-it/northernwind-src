package it.tidalwave.northernwind.infoglueexporter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    static abstract class Parser
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
            
            addResource(new Resource(dateTime, path + fileName, builder.toString().getBytes("UTF-8")));
          }
        
        protected void log (final @Nonnull String format, final @Nonnull Object ... args)
          {
            System.err.printf(spaces.substring(0, indent * 2) + format + "\n", args);
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    static class ContentParser extends Parser
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
            if ("attributes".equals(name))
              {
                inAttributes = false;  
              }
            else if (inAttributes)
              {
                if (Arrays.asList("FullText", "Template").contains(name))
                  {
                    // FIXME: format HTML
                    addResource(new Resource(dateTime, path + name + "_" + language + ".html", builder.toString().getBytes("UTF-8")));
//                    addResource(new Resource(dateTime, path + name + ".html", dumpXml("<body>" + builder.toString() + "</body>")));
                  }
                else
                  {
                    properties.put(name, builder.toString()); 
                  }
              }
          }        
        
        @Override
        protected void finish()
          throws UnsupportedEncodingException
          {
            dumpPropertiesAsResourceBundle("Resource_" + language + ".properties");
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    static class ComponentParser extends Parser
      {
        private final StringBuilder builder = new StringBuilder();
        private final SortedMap<String, String> properties;
        private String beanName = "";
        
        public ComponentParser (final @Nonnull String xml, 
                                final @Nonnull DateTime dateTime, 
                                final @Nonnull String path,
                                final @Nonnull SortedMap<String, String> properties) 
          {
            super(xml, path, dateTime);
            this.properties = properties;
          }
        
        @Override
        protected void processStartElement (final @Nonnull String name,  final @Nonnull XMLStreamReader reader)
          throws Exception
          {
            if ("components".equals(name) && (indent == 0))
              {
                builder.append("<beans>");
              }
            else if ("components".equals(name) && (indent > 0))
              {
                builder.append("<property name='subcomponents'><list>");
              }
            else if ("component".equals(name))
              {
                builder.append("<bean ");
                
                for (int i = 0; i < reader.getAttributeCount(); i++)
                  {
                    final String attrName = reader.getAttributeName(i).getLocalPart();
                    final String attrValue = reader.getAttributeValue(i);
                    
                    if ("id".equals(attrName))
                      {
                        builder.append(String.format("id='%s' ", attrValue));
                      }
                    else if ("contentId".equals(attrName))
                      {
                        builder.append(String.format("class='%s' ", attrValue));
                      }
                  }
                
                builder.append(" >");
                
                for (int i = 0; i < reader.getAttributeCount(); i++)
                  {
                    final String attrName = reader.getAttributeName(i).getLocalPart();
                    final String attrValue = reader.getAttributeValue(i);
                    
                    if ("name".equals(attrName))
                      {
                        beanName = attrValue;
                      }
                    
                    if (!Arrays.asList("id", "contentId").contains(attrName))
                      {
                        builder.append(String.format("<property name='%s' value='%s'/>", attrName, attrValue));
                      }
                  }
              }
            else if ("property".equals(name))
              {
                for (int i = 0; i < reader.getAttributeCount(); i++)
                  {
                    final String attrName = reader.getAttributeName(i).getLocalPart();
                    final String attrValue = reader.getAttributeValue(i);
                    
                    if ("path".equals(attrName))
                      {
                        builder.append(String.format("<property name='content' value='${%s}'/>", beanName + ".content"));
                        properties.put(beanName + ".content", "/content/document/" + attrValue);
                      }
                  }
              }
          }
                
        @Override
        protected void processAttribute (final @Nonnull String name,  final @Nonnull XMLStreamReader reader)
          throws Exception
          {
            builder.append(String.format("<property name='%s' value='%s'/>", name, "bwlin"));
          }
        
        @Override
        protected void processEndElement (final @Nonnull String name)
          throws Exception
          {
            if ("components".equals(name) && (indent == 0))
              {
                builder.append("</beans>");
              }
            else if ("components".equals(name) && (indent > 0))
              {
                builder.append("</list></property>");
              }
            else if ("component".equals(name))
              {
                builder.append("</bean>");
              }
            else 
              {
//                builder.append("</").append(name).append(">");                   
              }
          }
        
        @Override
        protected void finish() 
          throws Exception
          {
            addResource(new Resource(dateTime, path, dumpXml(builder.toString())));
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    static class StructureParser extends Parser
      {
        private final String language;
        
        public StructureParser (String xml, final @Nonnull DateTime dateTime, String path, String language) 
          throws FileNotFoundException 
          {
            super(xml, path, dateTime);
            this.language = language;
          }
        
        @Override
        protected void processEndElement (final @Nonnull String name)
          throws Exception
          {
            if ("ComponentStructure".equals(name))
              {
                try
                  {
                    new ComponentParser(builder.toString(), dateTime, path + "beans_" + language + ".xml", properties).process();
                  }
                catch (Exception e)
                  {
                      System.err.println("ERROR: " + e + " ON " + builder);                        
                  }
              }
            else if (!"attributes".equals(name))
              {
                properties.put(name, builder.toString()); 
              }
          }
        
        @Override
        protected void finish() 
          throws UnsupportedEncodingException
          {
            dumpPropertiesAsResourceBundle("Resource_" + language + ".properties");
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    static class Resource
      {
        @Getter
        private final DateTime dateTime;

        private final String path;

        private final byte[] contents;

        // TODO: add an XML bag of properties (metadata.xml) with mime type and language list
        // XML must be formatted and fields sorted
        public void addAndCommit() 
          throws Exception
          {
            String fixedPath = this.path;
            final File file = new File(hgFolder, fixedPath);
            file.getParentFile().mkdirs();
            System.err.println("Writing " + file.getAbsolutePath() + "...");
            final OutputStream os = new FileOutputStream(file);
            os.write(contents);
            os.close();
            exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg add " + fixedPath);
            exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg commit -m \"...\" " + fixedPath + " --date \'" + dateTime.toDate().getTime() / 1000 + " 0\'");
          }
      }
    
    private static final File hgFolder = new File("/home/fritz/.nw/test/root");      
    
    private static final SortedMap<DateTime, List<Resource>> resourceMapByDateTime = new TreeMap<DateTime, List<Resource>>();
    
    private static final List<Resource> media = new ArrayList<Resource>();
    
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
        addAndCommitResources();
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
        exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg init");
        
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
                      media.add(new Resource(new DateTime(), fixedPath, decoder.decodeBuffer(builder.toString())));
                    }
                  
                  indent--;
                    
                default:
                  name = event.getName().toString();
                  System.err.printf("%s%d %s: %s\n",  spaces.substring(0, indent * 2), eventType, name, builder.substring(0, Math.min(1000, builder.length())));
                  break;
              }
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void addAndCommitResources() 
      throws Exception
      {        
        for (final List<Resource> resources : resourceMapByDateTime.values())
          {
              // FIXME: first add all of them, then commit all of them
            for (final Resource resource : resources)
              {
                resource.addAndCommit();  
              }
          }
        
        for (final Resource resource : media)
          {
            resource.addAndCommit();  
          }
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void addResource (final Resource resource)
      {
        List<Resource> resources = resourceMapByDateTime.get(resource.getDateTime());
        
        if (resources == null)
          {
            resources = new ArrayList<Resource>();
          }
        
        resources.add(resource);
        resourceMapByDateTime.put(resource.getDateTime(), resources);
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static void exec (String ... args) 
      throws Exception
      {
        System.err.println(Arrays.toString(args));
        Runtime.getRuntime().exec(args).waitFor();
      }
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    private static byte[] dumpXml (final @Nonnull String string)
      throws Exception
      {
        final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        final DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        final LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        final LSOutput output = impl.createLSOutput();
        final @Cleanup ByteArrayOutputStream os = new ByteArrayOutputStream();
        output.setByteStream(os);
        writer.write(parseXmlFile(string), output);
        os.close();
        return os.toByteArray();
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private static Document parseXmlFile (final @Nonnull String in)
      throws ParserConfigurationException, SAXException, IOException
      {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final InputSource is = new InputSource(new StringReader(in));
        return db.parse(is);
      }
  }
