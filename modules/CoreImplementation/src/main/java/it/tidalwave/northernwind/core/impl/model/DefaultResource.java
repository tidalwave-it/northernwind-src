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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.spi.ResourceSupport;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.northernwind.core.model.Resource.PROPERTY_PLACE_HOLDER;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;

/***********************************************************************************************************************
 *
 * The default implementation for {@link Resource}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(callSuper = true, of = "placeHolder")
/* package */ class DefaultResource extends ResourceSupport
  {
    @Inject
    private InheritanceHelper inheritanceHelper;

    @Inject
    private RequestLocaleManager localeRequestManager;

    private final Map<Locale, ResourceProperties> propertyMapByLocale = new HashMap<>();

    @Getter
    private boolean placeHolder;

    private final ResourceProperties.PropertyResolver propertyResolver;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResource (final @Nonnull Resource.Builder builder)
      {
        super(builder);
        propertyResolver = new TextResourcePropertyResolver(getFile());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getProperties()
      {
        return propertyMapByLocale.get(localeRequestManager.getLocales().get(0));
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void loadProperties()
      throws IOException
      {
        final ResourceFile file = getFile();
        log.debug("loadProperties() for {}", file.getPath().asString());

        boolean tmpPlaceHolder = true;

        for (final Locale locale : localeRequestManager.getLocales())
          {
            ResourceProperties properties = modelFactory.createProperties().withPropertyResolver(propertyResolver).build();

            if (file.isData())
              {
                tmpPlaceHolder = false;
              }
            else
              {
                for (final ResourceFile propertyFile : inheritanceHelper.getInheritedPropertyFiles(file, locale, "Properties"))
                  {
                    log.trace(">>>> reading properties from {} ({})...", propertyFile.getPath().asString(), locale);
                    @Cleanup final InputStream is = propertyFile.getInputStream();
                    final ResourceProperties tempProperties =
                        modelFactory.createProperties().build().as(Unmarshallable).unmarshal(is);
        //                modelFactory.createProperties().withPropertyResolver(propertyResolver).build().as(Unmarshallable).unmarshal(is);
                    log.trace(">>>>>>>> read properties: {} ({})", tempProperties, locale);
                    properties = properties.merged(tempProperties);
                    tmpPlaceHolder &= !propertyFile.getParent().equals(file);
                  }
              }

            placeHolder = properties.getBooleanProperty(PROPERTY_PLACE_HOLDER, tmpPlaceHolder);

            if (log.isDebugEnabled())
              {
                log.debug(">>>> properties for {} ({}):", file.getPath().asString(), locale);
                logProperties(">>>>>>>>", properties);
              }

            propertyMapByLocale.put(locale, properties);
          }
      }

    /*******************************************************************************************************************
     *
     * FIXME: move to ResourceProperties!
     *
     ******************************************************************************************************************/
    private void logProperties (final @Nonnull String indent,
                                final @Nonnull ResourceProperties properties)
      {
        log.debug("{} property items:", indent);

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
