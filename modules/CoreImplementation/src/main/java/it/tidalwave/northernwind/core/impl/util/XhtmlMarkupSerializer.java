/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - hg clone https://bitbucket.org/tidalwave/northernwind-src
 * %%
 * Copyright (C) 2011 - 2014 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.core.impl.util;

import it.tidalwave.northernwind.core.impl.patches.XHTMLSerializer;
import java.util.Arrays;
import java.io.Writer;
import java.io.IOException;
import org.xml.sax.SAXException;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
// FIXME: it makes use of a deprecated class but it's needed by NW-96.
public class XhtmlMarkupSerializer extends XHTMLSerializer
  {
    private static final OutputFormat outputFormat = new OutputFormat("xhtml", "UTF-8", false);

    static
      {
        outputFormat.setPreserveSpace(true);
        outputFormat.setOmitXMLDeclaration(true);
      }

    public XhtmlMarkupSerializer (final Writer writer)
      {
        super(writer, outputFormat);
      }

    @Override
    public void characters (final char[] chars, final int start, final int length)
      throws SAXException
      {
        try
          {
            if (shouldOmitCDATA())
              {
                content(); // flushes element start
                printText(chars, start, length, true, true);
              }
            else
              {
                super.characters(chars, start, length);
              }
          }
        catch (IOException e)
          {
            throw new SAXException(e);
          }
      }

    @Override
    protected void characters (final String text)
      {
        try
          {
            if (shouldOmitCDATA())
              {
                content(); // flushes element start
                printText(text, true, true);
              }
            else
              {
                super.characters(text);
              }
          }
        catch (IOException e)
          {
            throw new RuntimeException(e);
          }
      }

    private boolean shouldOmitCDATA()
      {
        return Arrays.asList("script", "style").contains(getElementState().rawName);
      }

    @Override
    protected String getEntityRef (final int ch)
      {
        return ((ch == '\n') || (ch == '\r')) ? "#10" : null;
      }
  }
