/***********************************************************************************************************************
 *
 * PROJECT NAME
 * PROJECT COPYRIGHT
 *
 ***********************************************************************************************************************
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
 ***********************************************************************************************************************
 *
 * WWW: PROJECT URL
 * SCM: PROJECT SCM
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.importer.infoglue;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ExportConverter extends Converter 
  {
    public ExportConverter (final @Nonnull InputStream is)
      throws XMLStreamException 
      {
        super(is);
      }

    @Override
    protected void processStartElement (final @Nonnull String elementName, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("root-content".equals(elementName))
          {  
            new ExportContentConverter(this).process();
            localLevel--; // FIXME: doesn't properly receive the endElement for this
          }
        if ("root-site-node".equals(elementName))
          {  
            new ExportSiteNodeConverter(this).process();
            localLevel--; // FIXME: doesn't properly receive the endElement for this
          }
      }

    @Override
    protected void start() throws Exception
      {
        ResourceManager.initialize();
      }
    
    @Override
    protected void processEndElement (final @Nonnull String elementName) 
      throws Exception
      {
      }

    @Override
    protected void finish() throws Exception 
      {
        ResourceManager.addAndCommitResources();
        ResourceManager.tagConversionCompleted();
      }
  } 
