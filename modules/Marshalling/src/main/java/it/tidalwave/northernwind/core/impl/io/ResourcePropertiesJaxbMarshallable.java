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
package it.tidalwave.northernwind.core.impl.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.TreeSet;
import java.io.IOException;
import java.io.OutputStream;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.role.io.Marshallable;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import it.tidalwave.northernwind.core.impl.io.jaxb.ObjectFactory;
import it.tidalwave.northernwind.core.impl.io.jaxb.PropertiesJaxb;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@DciRole(datumType = ResourceProperties.class) @Configurable @Slf4j
public class ResourcePropertiesJaxbMarshallable implements Marshallable
  {
    @Nonnull
    private final ResourceProperties resourceProperties;

    @Inject
    private ObjectFactory objectFactory;

    @Inject
    private Marshaller marshaller;

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public ResourcePropertiesJaxbMarshallable (@Nonnull final ResourceProperties resourceProperties)
      {
        this.resourceProperties = resourceProperties;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override
    public void marshal (@Nonnull final OutputStream os)
      throws IOException
      {
        try
          {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // FIXME: set in Spring
            marshaller.marshal(objectFactory.createProperties(marshal(resourceProperties)), os);
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
    private PropertiesJaxb marshal (@Nonnull final ResourceProperties properties)
      {
        final var propertiesJaxb = objectFactory.createPropertiesJaxb();
        final var id = properties.getId();

        if ("".equals(id.stringValue()))
          {
            propertiesJaxb.setVersion("1.0");
          }
        else
          {
            propertiesJaxb.setId(id.stringValue());
          }

        for (final var key : new TreeSet<>(properties.getKeys()))
          {
            final var propertyJaxb = objectFactory.createPropertyJaxb();
            propertyJaxb.setName(key.stringValue());
            properties.getProperty(key).ifPresent(value ->
              {
                if (value instanceof Collection)
                  {
                    final var valuesJaxb = objectFactory.createValuesJaxb();
                    propertyJaxb.setValues(valuesJaxb);

                    for (final Object valueItem : (Collection<?>)value)
                      {
                        valuesJaxb.getValue().add(valueItem.toString());
                      }
                  }
                else
                  {
                    propertyJaxb.setValue(value.toString());
                  }

                propertiesJaxb.getProperty().add(propertyJaxb);
              });
          }

        for (final var groupId : new TreeSet<>(properties.getGroupIds()))
          {
            propertiesJaxb.getProperties().add(marshal(properties.getGroup(groupId)));
          }

        return propertiesJaxb;
     }
  }
