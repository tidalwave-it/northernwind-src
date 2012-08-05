/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://tidalwave.it)
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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.io.IOException;

/***********************************************************************************************************************
 *
 * A provider for the {@link ResourceFileSystem}.
 * FIXME: possibly drop the need for a Provider - instantiate filesystems directly.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface ResourceFileSystemProvider
  {
    /*******************************************************************************************************************
     *
     * Returns the {@link ResourceFileSystem}.
     * 
     * @return  the {@code ResourceFileSystem}
     *
     ******************************************************************************************************************/
    @Nonnull
    public ResourceFileSystem getFileSystem()
      throws IOException;
  }