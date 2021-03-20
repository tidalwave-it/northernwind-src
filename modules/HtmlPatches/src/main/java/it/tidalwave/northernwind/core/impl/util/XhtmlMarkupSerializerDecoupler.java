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
package it.tidalwave.northernwind.core.impl.util;

import java.io.IOException;
import java.io.StringWriter;
import lombok.NoArgsConstructor;
import javax.annotation.Nonnull;
import org.w3c.dom.Node;
import it.tidalwave.northernwind.core.impl.patches.XHTMLSerializer;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE)
public final class XhtmlMarkupSerializerDecoupler extends XHTMLSerializer
  {
    public static void serialize (final @Nonnull Node node, final @Nonnull StringWriter stringWriter)
        throws IOException
      {
        final XhtmlMarkupSerializer xhtmlSerializer = new XhtmlMarkupSerializer(stringWriter);
        xhtmlSerializer.serialize(node);
      }
  }
