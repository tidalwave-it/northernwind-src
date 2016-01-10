/*
 * #%L
 * *********************************************************************************************************************
 * 
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.importer.infoglue;

import java.io.IOException;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ExportDigitalAssetsConverter extends Converter
  {
    private String assetFileName;

    private String assetKey;

    private byte[] assetBytes;

    private final ExportContentsVersionConverter parent;

    private final boolean onlyMapAssets;

    public ExportDigitalAssetsConverter (final @Nonnull ExportContentsVersionConverter parent, final boolean onlyMapAssets)
      {
        super(parent);
        this.parent = parent;
        this.onlyMapAssets = onlyMapAssets;
      }

    @Override
    protected void processEndElement (final @Nonnull String elementName)
      throws Exception
      {
        if ("assetFileName".equals(elementName))
          {
            assetFileName = contentAsString();
          }
        else if ("assetKey".equals(elementName))
          {
            assetKey = contentAsString();
          }
        else if ("assetBytes".equals(elementName))
          {
            assetBytes = contentAsBytes();
          }
      }

    @Override
    public void finish()
      {
        try
          {
            final String fixedPath = "/content/media/" + assetFileName;
            Main.assetFileNameMapByKey.put(assetKey, assetFileName);

            if (!onlyMapAssets)
              {
                log.info("Converting {} ...", fixedPath);
                ResourceManager.addCommand(new AddResourceCommand(parent.getModifiedDateTime(),
                                                                  fixedPath,
                                                                  contentAsBytes(), // FIXME: assetBytes?
                                                                  parent.getVersionComment()));
              }
          }
        catch (IOException e)
          {
          }
      }
  }
