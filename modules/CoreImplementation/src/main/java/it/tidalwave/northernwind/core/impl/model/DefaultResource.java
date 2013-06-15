/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(of = {"file", "placeHolder"})
/* package */ class DefaultResource implements Resource
  {
    @Inject @Nonnull
    private RequestLocaleManager localeRequestManager;

    @Inject @Nonnull
    private Provider<FilterSetExpander> macroExpander;

    @Inject @Nonnull
    private InheritanceHelper inheritanceHelper;

    @Nonnull @Getter
    private final ResourceFile file;

    @Nonnull @Getter
    private ResourceProperties properties;

    @Getter
    private boolean placeHolder;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private DefaultResourceProperties.PropertyResolver propertyResolver = new DefaultResourceProperties.PropertyResolver()
      {
        @Override @SuppressWarnings("unchecked")
        public <Type> Type resolveProperty (final @Nonnull Id propertyGroupId, final @Nonnull Key<Type> key)
          throws NotFoundException, IOException
          {
            return (Type)getFileBasedProperty(key.stringValue()); // FIXME: use also Id for SiteNode?
          }
      };

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResource (final @Nonnull ResourceFile file)
      {
        this.file = file;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getPropertyGroup (final @Nonnull Id id)
      {
        return properties.getGroup(id);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getFileBasedProperty (final @Nonnull String propertyName)
      throws NotFoundException, IOException
      {
        log.trace("getFileBasedProperty({})", propertyName);

        final ResourceFile propertyFile = findLocalizedFile(propertyName);
        log.trace(">>>> reading from {}", propertyFile.getPath());
        final String charset = propertyFile.getMimeType().equals("application/xhtml+xml") ? "UTF-8" : Charset.defaultCharset().name();

        try
          {
            return macroExpander.get().filter(propertyFile.asText(charset), propertyFile.getMimeType());
          }
        catch (RuntimeException e) // FIXME: introduce a FilterException
          {
            throw new IOException(e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void loadProperties()
      throws IOException
      {
        log.trace("loadProperties() for {}", file.getPath().asString());
        boolean tmpPlaceHolder = true;

        properties = new DefaultResourceProperties(new Id(""), propertyResolver);

        for (final ResourceFile propertyFile : inheritanceHelper.getInheritedPropertyFiles(file, "Properties_en.xml"))
          {
            log.trace(">>>> reading properties from {}...", propertyFile.getPath().asString());
            @Cleanup final InputStream is = propertyFile.getInputStream();
            final ResourceProperties tempProperties = new DefaultResourceProperties(propertyResolver).as(Unmarshallable).unmarshal(is);
            log.trace(">>>>>>>> read properties: {}", tempProperties);
            properties = properties.merged(tempProperties);
            tmpPlaceHolder &= !propertyFile.getParent().equals(file);
          }

        placeHolder = Boolean.parseBoolean(properties.getProperty(PROPERTY_PLACE_HOLDER, "" + tmpPlaceHolder));

        if (log.isDebugEnabled())
          {
            log.debug(">>>> properties for {}:", file.getPath().asString());
            logProperties(">>>>>>>>", properties);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceFile findLocalizedFile (final @Nonnull String fileName)
      throws NotFoundException
      {
        log.trace("findLocalizedFile({})", fileName);
        ResourceFile localizedFile = null;
        final StringBuilder fileNamesNotFound = new StringBuilder();
        String separator = "";

        for (final Locale locale : localeRequestManager.getLocales())
          {
            final String localizedFileName = fileName.replace(".", "_" + locale.getLanguage() + ".");
            localizedFile = file.getChildByName(localizedFileName);

            if ((localizedFile == null) && localizedFileName.endsWith(".xhtml"))
              {
                localizedFile = file.getChildByName(localizedFileName.replaceAll("\\.xhtml$", ".html"));
              }

            if (localizedFile != null)
              {
                break;
              }

            fileNamesNotFound.append(separator);
            fileNamesNotFound.append(localizedFileName);
            separator = ",";
          }

        return NotFoundException.throwWhenNull(localizedFile,
                    String.format("findLocalizedFile(): %s/{%s}", file.getPath().asString(), fileNamesNotFound));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void logProperties (final @Nonnull String indent ,final @Nonnull ResourceProperties properties)
      {
        log.debug("{} simple property items:", indent);

        for (final Key<?> key : properties.getKeys())
          {
            try
              {
                log.debug("{}>>>> {} = {}", indent, key, properties.getProperty(key));
              }
            catch (NotFoundException | IOException e)
              {
                log.error("", e);
              }
          }

        log.debug("{} property groups: {}", indent, properties.getGroupIds());

        for (final Id groupId : properties.getGroupIds())
          {
            log.debug("{}>>>> group: {}", indent, groupId);
            logProperties(indent + ">>>>", properties.getGroup(groupId));
          }
      }
  }
