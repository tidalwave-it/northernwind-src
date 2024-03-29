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
package it.tidalwave.northernwind.frontend.ui.component.htmltemplate;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.stringtemplate.v4.ST;
import it.tidalwave.util.Id;
import it.tidalwave.northernwind.core.model.SiteProvider;
import it.tidalwave.northernwind.frontend.ui.SiteViewController;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The HtmlTemplate specialization of {@link SiteViewController}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(exclude="siteProvider")
public class TextHolder
  {
    private final Map<String, String> attributeMap = new HashMap<>();

    private final List<TextHolder> contents = new ArrayList<>();

    @Getter @Setter
    private String template;

    @Inject
    private Provider<SiteProvider> siteProvider;

    @Getter @Setter
    private String mimeType = "text/plain";

    /*******************************************************************************************************************
     *
     * Creates an instance with the given name.
     *
     * @param  name  the component name
     *
     ******************************************************************************************************************/
    public TextHolder (@Nonnull final Id id)
      {
        attributeMap.put("content", "");

        try
          {
            loadTemplate();
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    public TextHolder (@Nonnull final String html)
      {
        addAttribute("content", html);
        template = "$content$";
      }

//    @Override
    public void setContent (@Nonnull final String content)
      {
        addAttribute("content", content);
//        setValue(html);
      }

    public void addComponent (@Nonnull final TextHolder child)
      {
        contents.add(child);
      }

    public void addAttribute (@Nonnull final String name, @Nonnull final String value)
      {
        attributeMap.put(name, value);
      }

//    @Nonnull
//    public byte[] asBytes2 (final @Nonnull Charset charset)
//      {
//        return asString2(charset).getBytes(charset);
//      }
//
//    @Nonnull
//    public String asString2 (final @Nonnull Charset charset)
//      {
//        return template;
//      }

    @Nonnull
    public byte[] asBytes (@Nonnull final Charset charset)
      {
        return asString(charset).getBytes(charset);
      }

    @Nonnull
    public String asString (@Nonnull final Charset charset)
      {
        var t = new ST(template, '$', '$');

        for (final var entry : attributeMap.entrySet())
          {
            t = t.add(entry.getKey(), entry.getValue());
          }

        final var builder = new StringBuilder();

        for (final var child : contents)
          {
            builder.append(child.asString(charset)).append("\n");
          }

        t = t.add("content",     builder.toString());
//        t = t.add("contextPath", siteProvider.get().getSite().getContextPath());
        t = t.add("charset",     charset.name());
//        t = t.add("language",    "");

        return t.render();
      }

    private void loadTemplate() // FIXME: use TemplateHelper
      throws IOException
      {
        // FIXME: this should be done only once...
        Resource resource = null;

        for (Class<?> clazz = getClass(); clazz.getSuperclass() != null; clazz = clazz.getSuperclass())
          {
            final var templateName = clazz.getSimpleName() + ".txt";
            resource = new ClassPathResource(templateName, clazz);

            if (resource.exists())
              {
                break;
              }
          }

        try
          {
            if (resource == null)
              {
                throw new FileNotFoundException();
              }

            try (final Reader r = new InputStreamReader(resource.getInputStream()))
              {
                final var charBuffer = CharBuffer.allocate((int)resource.contentLength());
                final var length = r.read(charBuffer);
                template = new String(charBuffer.array(), 0, length);
              }
          }
        catch (FileNotFoundException e) // no specific template, fallback
          {
            log.warn("No template for {}, using default", getClass().getSimpleName());
            template = "$content$\n";
          }
      }
  }
