/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.stringtemplate.v4.ST;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.SiteFinder;
import lombok.extern.slf4j.Slf4j;
import org.openide.filesystems.FileObject;
import static org.springframework.core.Ordered.*;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class XsltMacroFilter implements Filter
  {
    private static final String XSLT_TEMPLATES_PATH = "/XsltTemplates/.*";

    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    
    @Inject
    private Site site;
    
    private String xslt = "";
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @PostConstruct
    public void initialize()
      throws IOException
      {
        log.info("Retrieving XSLT templates");
        final String template = IOUtils.toString(getClass().getResourceAsStream("/it/tidalwave/northernwind/core/impl/model/XsltTemplate.xslt"));
        final StringBuilder xsltBuffer = new StringBuilder();
        SiteFinder<Resource> find = site.find(Resource.class);
        
        for (final Resource resource : site.find(Resource.class).withRelativePath(XSLT_TEMPLATES_PATH).results())
          {
            final FileObject file = resource.getFile();
            log.info(">>>> /{}", file.getPath());
            xsltBuffer.append(file.asText("UTF-8")); 
          }
        
        final ST t = new ST(template, '$', '$').add("content", xsltBuffer.toString());
        xslt = t.render();
      }
            
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String filter (final @Nonnull String text, final @Nonnull String mimeType) 
      {
        if (!mimeType.equals("application/xhtml+xml"))
          {
            log.warn("Cannot filter resources not in XHTML");
            return text;  
          }
        
        try
          {
            final Source transformation = new StreamSource(new StringReader(xslt));
            final Transformer transformer = transformerFactory.newTransformer(transformation); 
            final StringWriter stringWriter = new StringWriter();
            final Result result = new StreamResult(stringWriter);
            transformer.transform(new DOMSource(stringToNode(text)), result); 
            return stringWriter.toString().replace(" xmlns=\"\"", ""); // FIXME:
          }
        catch (SAXParseException e)
          {
            log.error("XML parse error: {} at l{}:c{}", new Object[] { e.getMessage(), e.getLineNumber(), e.getColumnNumber() });
            log.error(text);
            throw new RuntimeException(e);
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
        catch (SAXException e)
          {
            throw new RuntimeException(e);
          }
        catch (ParserConfigurationException e)
          {
            throw new RuntimeException(e);
          }
        catch (TransformerException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Node stringToNode (final @Nonnull String string) 
      throws IOException, SAXException, ParserConfigurationException 
      {
        factory.setValidating(false);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final InputSource source = new InputSource(new StringReader(string));
        final Document document = builder.parse(source);
        return document;
      }
  }
