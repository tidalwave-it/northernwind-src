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
import java.io.IOException;
import java.io.OutputStream;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.role.io.Marshallable;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.northernwind.frontend.ui.Layout;
import it.tidalwave.northernwind.core.impl.io.jaxb.ComponentJaxb;
import it.tidalwave.northernwind.core.impl.io.jaxb.ObjectFactory;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@DciRole(datumType = Layout.class) @Configurable @Slf4j
public class LayoutJaxbMarshallable implements Marshallable
  {
    @Nonnull
    private final Layout ownerLayout;

    @Inject
    private ObjectFactory objectFactory;

    @Inject
    private Marshaller marshaller;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public LayoutJaxbMarshallable (@Nonnull final Layout ownerLayout)
      {
        this.ownerLayout = ownerLayout;
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
            final var componentsJaxb = objectFactory.createComponentsJaxb();
            componentsJaxb.setVersion("1.0");
            componentsJaxb.setComponent(marshal(ownerLayout));
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // FIXME: set in Spring
            marshaller.marshal(objectFactory.createComponents(componentsJaxb), os);
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
    private ComponentJaxb marshal (@Nonnull final Layout layout)
      {
        final var componentJaxb = objectFactory.createComponentJaxb();
        componentJaxb.setId(layout.getId().stringValue());
        componentJaxb.setType(layout.getTypeUri());

        for (final var child : layout.findChildren().results())
          {
            componentJaxb.getComponent().add(marshal(child));
          }

        return componentJaxb;
      }
  }
