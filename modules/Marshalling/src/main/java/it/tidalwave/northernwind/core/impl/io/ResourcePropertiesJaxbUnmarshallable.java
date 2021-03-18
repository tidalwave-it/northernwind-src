/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.role.io.Unmarshallable;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.impl.io.jaxb.PropertiesJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.PropertyJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.ValuesJaxb;
import it.tidalwave.northernwind.core.model.ModelFactory;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@DciRole(datumType = ResourceProperties.class) @Configurable @RequiredArgsConstructor @Slf4j
public class ResourcePropertiesJaxbUnmarshallable implements Unmarshallable
  {
    @Nonnull
    private final ResourceProperties owner;

    @Inject
    private ModelFactory modelFactory;

    @Inject
    private Unmarshaller unmarshaller;

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public ResourceProperties unmarshal (final @Nonnull InputStream is)
      throws IOException
      {
        try
          {
            final PropertiesJaxb propertiesJaxb = ((JAXBElement<PropertiesJaxb>)unmarshaller.unmarshal(is)).getValue();

            if (!"1.0".equals(propertiesJaxb.getVersion()))
              {
                throw new IOException("Unexpected version: " + propertiesJaxb.getVersion());
              }

            return unmarshal(propertiesJaxb);
          }
        catch (JAXBException e)
          {
            throw new IOException("", e);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResourceProperties unmarshal (final @Nonnull PropertiesJaxb propertiesJaxb)
      {
        final Id id = new Id((propertiesJaxb.getId() != null) ? propertiesJaxb.getId() : "");
        ResourceProperties properties = modelFactory.createProperties().withId(id).build();

        for (final PropertyJaxb propertyJaxb : propertiesJaxb.getProperty())
          {
            final ValuesJaxb values = propertyJaxb.getValues();
            properties = properties.withProperty(Key.of(propertyJaxb.getName()),
                                                 (values != null) ? values.getValue() : propertyJaxb.getValue());
          }

        for (final PropertiesJaxb propertiesJaxb2 : propertiesJaxb.getProperties())
          {
            properties = properties.withProperties(unmarshal(propertiesJaxb2));
          }

        return properties;
      }
  }
