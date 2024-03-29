/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.core.model.Template.Aggregate;
import it.tidalwave.northernwind.core.impl.model.Filter;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static it.tidalwave.northernwind.core.model.Resource._Resource_;
import static it.tidalwave.northernwind.core.model.Template.Aggregates.toAggregates;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Order(HIGHEST_PRECEDENCE) @Slf4j
public class XsltMacroFilter implements Filter
  {
    private static final String XSLT_TEMPLATES_PATH = "/XsltTemplates/.*";

    private static final String DOCTYPE_HTML = "<!DOCTYPE html>";

    @Inject
    private ApplicationContext context;

    @Inject
    private DocumentBuilderFactory factory;

    @Inject
    private TransformerFactory transformerFactory;

    @Inject
    private Provider<SiteProvider> siteProvider;

    private String xslt = "";

    private volatile boolean initialized;

    private Method serializerMethod;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    // FIXME: this should be shared between instances
    private void initialize()
      {
        log.info("Retrieving XSLT templates");
        final var site = siteProvider.get().getSite();
        final var macros = site.find(_Resource_).withRelativePath(XSLT_TEMPLATES_PATH)
                               .stream()
                               .map(Resource::getFile)
                               .map(f -> Aggregate.of("body", asText(f)).with("name", f.getPath()))
                               .collect(toAggregates("macros"));
        xslt = site.getTemplate(getClass(), Optional.empty(), "XsltTemplate.xslt").render(macros);
        log.trace(">>>> xslt: {}", xslt);

        try
          {
            final var clazz = Thread.currentThread().getContextClassLoader().loadClass(
                    "it.tidalwave.northernwind.core.impl.util.XhtmlMarkupSerializerDecoupler");
            serializerMethod = clazz.getMethod("serialize", Node.class, StringWriter.class);
          }
        catch (ClassNotFoundException | NoSuchMethodException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public String filter (@Nonnull final String text, @Nonnull final String mimeType)
      {
        if (!"application/xhtml+xml".equals(mimeType))
          {
            log.debug("Cannot filter resources not in XHTML: {}", mimeType);
            return text;
          }

        // FIXME: buggy and cumbersome
        if (!initialized)
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

        try
          {
            final var result = new DOMResult();
            final var transformer = createTransformer();
            // Fix for NW-100
            transformer.transform(new DOMSource(stringToNode(text.replace("xml:lang", "xml_lang"))), result);

            final var stringWriter = new StringWriter();

            if (text.startsWith(DOCTYPE_HTML))
              {
                stringWriter.append(DOCTYPE_HTML).append("\n");
              }

            // Fix for NW-96
            // This must be accessed by reflection because the JDK 11+ compiler with --source 11 refuses to compile
            // stuff that depends on com.sun.* classes.
            serializerMethod.invoke(null, result.getNode(), stringWriter);
            return stringWriter.toString().replace("xml_lang", "xml:lang").replace(" xmlns=\"\"", ""); // FIXME:
          }
        catch (SAXParseException e)
          {
            log.error("XML parse error: {} at l{}:c{}", e.getMessage(), e.getLineNumber(), e.getColumnNumber());
            log.error(text);
            throw new RuntimeException(e);
          }
        catch (TransformerException e)
          {
            log.error("XSL error: {} at {}", e, e.getLocationAsString());
            log.error(xslt);
            throw new RuntimeException(e);
          }
        catch (IOException | SAXException | ParserConfigurationException | IllegalAccessException | InvocationTargetException e)
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
        final var transformer = transformerFactory.newTransformer(transformation);

        try
          {
            final var uriResolver = context.getBean(URIResolver.class);
            log.trace("Using URIResolver: {}", uriResolver.getClass());
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
    private static String asText (@Nonnull final ResourceFile file)
      {
        try
          {
            log.info(">>>> {}", file.getPath().asString());
            return file.asText("UTF-8");
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Node stringToNode (@Nonnull final String string)
      throws IOException, SAXException, ParserConfigurationException
      {
        factory.setValidating(false);
        final var builder = factory.newDocumentBuilder();
        final var source = new InputSource(new StringReader(string));
        return builder.parse(source);
      }
  }
