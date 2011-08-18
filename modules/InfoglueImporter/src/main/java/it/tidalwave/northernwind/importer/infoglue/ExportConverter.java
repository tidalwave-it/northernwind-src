/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.importer.infoglue;

import javax.annotation.Nonnull;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.joda.time.DateTime;
import org.apache.commons.io.IOUtils;
import lombok.Cleanup;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class ExportConverter extends Converter 
  {
    private final boolean onlyMapAssets;
    
    public ExportConverter (final @Nonnull InputStream is, final boolean onlyMapAssets)
      throws XMLStreamException 
      {
        super(is);
        this.onlyMapAssets = onlyMapAssets;
      }

    @Override
    protected void processStartElement (final @Nonnull String elementName, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
        if ("root-content".equals(elementName))
          {  
            new ExportContentConverter(this, onlyMapAssets).process();
            localLevel--; // FIXME: doesn't properly receive the endElement for this
          }
        
        else if ("root-site-node".equals(elementName) && !onlyMapAssets)
          {  
            new ExportSiteNodeConverter(this).process();
            localLevel--; // FIXME: doesn't properly receive the endElement for this
          }
      }

    @Override
    protected void start() throws Exception
      {
        if (!onlyMapAssets)
          {
            ResourceManager.initialize();
          }
      }
    
    @Override
    protected void processEndElement (final @Nonnull String elementName) 
      throws Exception
      {
      }

    @Override
    protected void finish() throws Exception 
      {
        if (!onlyMapAssets)
          {
            final DateTime dateTime = ResourceManager.getTimeBase().minusSeconds(1);
            addLibraries(Main.zipLibraryFile, dateTime);

            ResourceManager.addAndCommitResources();
            ResourceManager.tagConversionCompleted();
          }
      }
    
    protected void addLibraries (final @Nonnull String zippedLibraryPath, final @Nonnull DateTime dateTime)
      throws IOException
      {
        final ZipFile zipFile = new ZipFile(zippedLibraryPath);
        final Enumeration enumeration = zipFile.entries();

        while (enumeration.hasMoreElements())
          {
            final ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
            
            if (!zipEntry.isDirectory())
              {
    //                System.out.println("Unzipping: " + zipEntry.getName());
                final @Cleanup InputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                ResourceManager.addCommand(new AddResourceCommand(dateTime,
                                                                  zipEntry.getName(), 
                                                                  IOUtils.toByteArray(is), 
                                                                  "Extracted from library"));
              }
          }
      }
  } 
