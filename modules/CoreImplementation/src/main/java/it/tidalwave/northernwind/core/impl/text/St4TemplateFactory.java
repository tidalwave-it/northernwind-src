/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.text;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.stringtemplate.v4.ST;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import it.tidalwave.northernwind.core.model.Site;
import it.tidalwave.northernwind.core.model.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Content.*;
import it.tidalwave.northernwind.core.model.ResourcePath;

/***********************************************************************************************************************
 *
 * A factory of {@link Template} implementations based on StringTemplate ({@link ST}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Slf4j
public class St4TemplateFactory
  {
    @Nonnull
    private final Class<?> clazz;

    @Nonnull
    private final Site site;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Template getTemplate (final @Nonnull Optional<ResourcePath> templatePath,
                                 final @Nonnull String embeddedResourceName)
      {
        log.debug("getTemplate({}, {})", templatePath, embeddedResourceName);
        final String text = templatePath.flatMap(this::getTemplate)
                                        .orElseGet(() -> getEmbeddedTemplate(embeddedResourceName));
        final char delimiter = embeddedResourceName.endsWith(".xslt") ? '%' : '$';
        // TODO: use a cache. Implement it on Site (as a generic cache), so it gets resetted when the Site is reset.
        return new St4Template(text, delimiter);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public Optional<String> getTemplate (final @Nonnull ResourcePath templatePath)
      {
        log.debug("getTemplate({})", templatePath);
        return site.find(Content).withRelativePath(templatePath).optionalResult().flatMap(c -> c.getProperty(P_TEMPLATE));
      }

    /*******************************************************************************************************************
     *
     * Gets an embedded, default template with the given name. The template file should be in the classpath, in the same
     * package as the owner class.
     *
     * @param       fileName            the name of the file
     * @return                          the template contents
     * @throws      RuntimeException    if the template can't be loaded
     *
     ******************************************************************************************************************/
    @Nonnull
    /* visible for testing */ String getEmbeddedTemplate (final @Nonnull String fileName)
      {
        final String packagePath = clazz.getPackage().getName().replace('.', '/');
        final Resource resource = new ClassPathResource("/" + packagePath + "/" + fileName);

        try (final Reader r = new InputStreamReader(resource.getInputStream()))
          {
            final char[] buffer = new char[(int)resource.contentLength()];
            r.read(buffer);
            return new String(buffer);
          }
        catch (IOException e)
          {
            throw new RuntimeException("Missing resource: " + fileName, e);
          }
      }
  }
