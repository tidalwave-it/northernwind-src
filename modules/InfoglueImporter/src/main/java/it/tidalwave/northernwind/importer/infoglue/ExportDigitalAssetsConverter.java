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
    
    public ExportDigitalAssetsConverter (final @Nonnull ExportContentsVersionConverter parent)
      {
        super(parent);        
        this.parent = parent;
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
            log.info("Converting {} ...", fixedPath);
            ResourceManager.addCommand(new AddResourceCommand(parent.getModifiedDateTime(), fixedPath, contentAsBytes()));
          } 
        catch (IOException e) 
          {
          }
      }
  }


//START ELEMENT 1 digitalAssets (0)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetFileName (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetFileName (2): blueBill_Mobile-Banner.png
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetKey (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetKey (2): blueBill_Mobile-Banner
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetFilePath (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetFilePath (2): /home/fgiudici/domains/tidalwave.it/subdomains/services/webapps/infoglueCMS/digitalAssets
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetContentType (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetContentType (2): image/png
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetFileSize (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetFileSize (2): 12638
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - START ELEMENT 1 assetBytes (1)
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 assetBytes (2): iVBORw0KGgoAAAANSUhEUgAAA+kAAABHCAIAAAD0oZErAAAACXBIWXMAAxhpAAMYaQGrnxC0AAAAIGNIUk0AAHoNAACAegAA7PAAAITJAACCmAAA2ecAADh4AAAvqKUImdQAADDkSURBVHja7N15eFTV2QDwc85dZstMVrKzBMQAEkCLAbG2Iqho60Jtq2KrVqvt90ltqdaKtVqrldpat6L9lGoLKrYupdiqoCBFREKIIgSSACEJCdkzS2a/957l++MMQ8iCERJU+v7K0+aZuffcc++d6fOeM+95LxZCIAAAAAAAAMDnHobYHQAAAAAAAIjdAQAAAAAAABC7AwAAAAAAALH7iWGaZiQSUQiJRqOxWJRxjjEmmNjt9hS3Wwhht9t1XYc7BAAAAAAAwGcQu3POu7u729taOzs6/H5/LBbrDgQQEoxxLoRAiCAkkEACuVwul8uVnplZWFiYk5uXlpaGMYa7BQAAAAAAIHYfdoZh1NfV1e7b19HeZphGdyAQDIXC4bARi1mWxYWQ3cAYE0I0VbXb7Q6n053idjidms2Wnpo67+KvZY0YMUzdY4yZpomQ4Fx0dXV2dHR4O7vOmzNHt9lOsvsthLCY4FwggfyheGtXhFJ25mm5MDQCAAAAAPj8U0/AMfbUVG/bWtbdHfT6vJ3t7cHubtOysIzTMUYYJwNHIQRjzDLNcCQiurqQELque9LSgsFgfkHBeXPPH/K+hcPhZ595GiGUlp6h61okHBZI+H2+5paWr5x77smUshOJmc//a6fdYfekODUFdYdiCOHObjMWDZZOzoNvAgAAAADAf3vsblnWmjffrKmpCgaDzU1N0WhUURRCiKr2OO6hSfeeZEyPEGKMdXZ0uFyu9e+8Uzhy1KnFxUPbQ7vd/tXZs1taWv69+p+GYaqaasSNWDQycvSYk2wiWlPJzMm5dQe9//lgl+ZKRxjHTRaL0YJUCl8DAAAAAIAvhGHMmWGMrXzxhbra2rbWFr/P
//21:55:46.002 [infoglue.Main.main()] TRACE i.t.northernwind.importer.infoglue.Converter       - END ELEMENT   2 digitalAssets (1): iVBORw0KGgoAAAANSUhEUgAAA+kAAABHCAIAAAD0oZErAAAACXBIWXMAAxhpAAMYaQGrnxC0AAAAIGNIUk0AAHoNAACAegAA7PAAAITJAACCmAAA2ecAADh4AAAvqKUImdQAADDkSURBVHja7N15eFTV2QDwc85dZstMVrKzBMQAEkCLAbG2Iqho60Jtq2KrVqvt90ltqdaKtVqrldpat6L9lGoLKrYupdiqoCBFREKIIgSSACEJCdkzS2a/957l++MMQ8iCERJU+v7K0+aZuffcc++d6fOeM+95LxZCIAAAAAAAAMDnHobYHQAAAAAAAIjdAQAAAAAAABC7AwAAAAAAALH7iWGaZiQSUQiJRqOxWJRxjjEmmNjt9hS3Wwhht9t1XYc7BAAAAAAAwGcQu3POu7u729taOzs6/H5/LBbrDgQQEoxxLoRAiCAkkEACuVwul8uVnplZWFiYk5uXlpaGMYa7BQAAAAAAIHYfdoZh1NfV1e7b19HeZphGdyAQDIXC4bARi1mWxYWQ3cAYE0I0VbXb7Q6n053idjidms2Wnpo67+KvZY0YMUzdY4yZpomQ4Fx0dXV2dHR4O7vOmzNHt9lOsvsthLCY4FwggfyheGtXhFJ25mm5MDQCAAAAAPj8U0/AMfbUVG/bWtbdHfT6vJ3t7cHubtOysIzTMUYYJwNHIQRjzDLNcCQiurqQELque9LSgsFgfkHBeXPPH/K+hcPhZ595GiGUlp6h61okHBZI+H2+5paWr5x77smUshOJmc//a6fdYfekODUFdYdiCOHObjMWDZZOzoNvAgAAAADAf3vsblnWmjffrKmpCgaDzU1N0WhUURRCiKr2OO6hSfeeZEyPEGKMdXZ0uFyu9e+8Uzhy1KnFxUPbQ7vd/tXZs1taWv69+p+GYaqaasSNWDQycvSYk2wiWlPJzMm5dQe9/