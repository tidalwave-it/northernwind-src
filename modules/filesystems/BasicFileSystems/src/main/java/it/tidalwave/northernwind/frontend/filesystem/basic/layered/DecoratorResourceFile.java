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
package it.tidalwave.northernwind.frontend.filesystem.basic.layered;

import javax.annotation.Nonnull;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.spi.DecoratedResourceFileSupport;
import lombok.Delegate;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * FIXME: this could probably be merged to the superclass.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@ToString(of="delegate")
class DecoratorResourceFile extends DecoratedResourceFileSupport<LayeredFileSystemProvider>
  {
    @Delegate(types=ResourceFile.class, excludes=FileDelegateExclusions.class) @Nonnull
    private final ResourceFile delegate;

    public DecoratorResourceFile (final @Nonnull LayeredFileSystemProvider fileSystemProvider, 
                                  final @Nonnull ResourceFile delegate) 
      {
        super(fileSystemProvider, delegate);
        this.delegate = delegate;
      }
  }    

