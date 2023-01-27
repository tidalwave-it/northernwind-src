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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.model.spi.ResourceSupport;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.io.Unmarshallable._Unmarshallable_;

/***********************************************************************************************************************
 *
 * The default implementation for {@link Resource}.
 *
 * @author  Fabrizio Giudici
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
    public DefaultResource (@Nonnull final Resource.Builder builder)
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
        final var file = getFile();
        log.debug("loadProperties() for {}", file.getPath().asString());

        var tmpPlaceHolder = true;

        for (final var locale : localeRequestManager.getLocales())
          {
            var properties = modelFactory.createProperties().withPropertyResolver(propertyResolver).build();

            if (file.isData())
              {
                tmpPlaceHolder = false;
              }
            else
              {
                for (final var propertyFile : inheritanceHelper.getInheritedPropertyFiles(file, locale, "Properties"))
                  {
                    log.trace(">>>> reading properties from {} ({})...", propertyFile.getPath().asString(), locale);
                    @Cleanup final var is = propertyFile.getInputStream();
                    final ResourceProperties tempProperties =
                        modelFactory.createProperties().build().as(_Unmarshallable_).unmarshal(is);
        //                modelFactory.createProperties().withPropertyResolver(propertyResolver).build().as(_Unmarshallable_).unmarshal(is);
                    log.trace(">>>>>>>> read properties: {} ({})", tempProperties, locale);
                    properties = properties.merged(tempProperties);
                    tmpPlaceHolder &= !propertyFile.getParent().equals(file);
                  }
              }

            placeHolder = properties.getProperty(P_PLACE_HOLDER).orElse(tmpPlaceHolder);

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
    private static void logProperties (@Nonnull final String indent,
                                       @Nonnull final ResourceProperties properties)
      {
        log.debug("{} property items:", indent);

        for (final var key : properties.getKeys())
          {
            log.debug("{}>>>> {} = {}", indent, key, properties.getProperty(key));
          }

        log.debug("{} property groups: {}", indent, properties.getGroupIds());

        for (final var groupId : properties.getGroupIds())
          {
            log.debug("{}>>>> group: {}", indent, groupId);
            logProperties(indent + ">>>>", properties.getGroup(groupId));
          }
      }
  }
