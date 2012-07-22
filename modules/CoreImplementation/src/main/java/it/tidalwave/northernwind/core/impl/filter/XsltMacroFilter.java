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
package it.tidalwave.northernwind.core.impl.filter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.stringtemplate.v4.ST;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.openide.filesystems.FileObject;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.impl.util.XhtmlMarkupSerializer;
import it.tidalwave.northernwind.core.impl.model.Filter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class XsltMacroFilter implements Filter
  {
    private static final String XSLT_TEMPLATES_PATH = "/XsltTemplates/.*";

    private static final String DOCTYPE_HTML = "<!DOCTYPE html>";
    
    @Inject @Nonnull
    private ApplicationContext context;
        
    @Inject @Nonnull
    private DocumentBuilderFactory factory;
        
    @Inject @Nonnull
    private TransformerFactory transformerFactory;
    
    @Inject @Nonnull
    private Provider<SiteProvider> siteProvider;
    
    private String xslt = "";
    
    private volatile boolean initialized = false;
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    // FIXME: this should be shared between instances
    private void initialize()
      throws IOException, NotFoundException
      {
        log.info("Retrieving XSLT templates");
        final String template = IOUtils.toString(getClass().getResourceAsStream("/it/tidalwave/northernwind/core/impl/filter/XsltTemplate.xslt"));
        final StringBuilder xsltBuffer = new StringBuilder();
        
        for (final Resource resource : siteProvider.get().getSite().find(Resource.class).withRelativePath(XSLT_TEMPLATES_PATH).results())
          {
            final FileObject file = resource.getFile();
            log.info(">>>> /{}", file.getPath());
            xsltBuffer.append(file.asText("UTF-8")); 
          }
        
        final ST t = new ST(template, '%', '%').add("content", xsltBuffer.toString());
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
           
        if (!initialized)
          {
            try
              {
                synchronized (this)
                  {
                    if (!initialized)
                      {
                        initialize();
                        initialized = true;
                      }
                  }
              }
            catch (IOException | NotFoundException e)
              {
                throw new RuntimeException(e);
              }
          }
        
        try
          {
            final DOMResult result = new DOMResult();
            final Transformer transformer = createTransformer();
            // Fix for NW-100
            transformer.transform(new DOMSource(stringToNode(text.replace("xml:lang", "xml_lang"))), result); 
            
            final StringWriter stringWriter = new StringWriter();
            
            if (text.startsWith(DOCTYPE_HTML))
              {
                stringWriter.append(DOCTYPE_HTML).append("\n");  
              }
            
            // Fix for NW-96
            final XhtmlMarkupSerializer xhtmlSerializer = new XhtmlMarkupSerializer(stringWriter);
            xhtmlSerializer.serialize(result.getNode());
            return stringWriter.toString().replace("xml_lang", "xml:lang").replace(" xmlns=\"\"", ""); // FIXME:
          }
        catch (SAXParseException e)
          {
            log.error("XML parse error: {} at l{}:c{}", new Object[] { e.getMessage(), e.getLineNumber(), e.getColumnNumber() });
            log.error(text);
            throw new RuntimeException(e);
          }
        catch (TransformerException e)
          {
            log.error("XSL error: {} at {}", e.toString(), e.getLocationAsString());
            log.error(xslt);
            throw new RuntimeException(e);
          }
        catch (IOException | SAXException | ParserConfigurationException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Transformer createTransformer() 
      throws TransformerConfigurationException
      {  
        final Source transformation = new StreamSource(new StringReader(xslt));
        final Transformer transformer = transformerFactory.newTransformer(transformation); 

        try
          {
            final URIResolver uriResolver = context.getBean(URIResolver.class);
            log.info("Using URIResolver: {}", uriResolver.getClass());
            transformer.setURIResolver(uriResolver);
          }
        catch (NoSuchBeanDefinitionException e)
          {
            // ok, not installed 
          }

        return transformer;
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
