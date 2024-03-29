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
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import it.tidalwave.northernwind.core.impl.model.mock.MockModelFactory;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class MockFileSystemWithOnlyIgnoredFiles extends EmptyMockFileSystem
  {
    public MockFileSystemWithOnlyIgnoredFiles()
      {
        super("File system with only ignored folders");
      }
    
    @Override
    public void setUp (@Nonnull final ResourceFileSystem fileSystem,
                       @Nonnull final MockModelFactory.PropertySetter propertySetter)
      {
        super.setUp(fileSystem, propertySetter);
        createMockFolder(fileSystem, documentFolder, "ignored1");
        createMockFolder(fileSystem, nodeFolder, "ignored2");
      }

    // performAssertions() same as superclass
  }
