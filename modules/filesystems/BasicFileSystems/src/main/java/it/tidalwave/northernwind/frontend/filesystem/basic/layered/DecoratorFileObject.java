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
import java.io.IOException;
import it.tidalwave.northernwind.core.model.NwFileObject;
import lombok.Delegate;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
class DecoratorFileObject extends FileObjectDelegateSupport
  {
    @Delegate(types=NwFileObject.class, excludes=FileDelegateExclusions.class) @Nonnull
    private final NwFileObject delegate;

    public DecoratorFileObject (final @Nonnull LayeredFileSystemProvider fileSystemProvider, 
                                final @Nonnull NwFileObject delegate) 
      {
        super(fileSystemProvider);
        this.delegate = delegate;
      }

    @Override
    public NwFileObject getParent()
      {
        return fileSystemProvider.createDecoratorFileObject(delegate.getParent());
      }

    @Override
    public NwFileObject createData (final String name, final String ext)
        throws IOException
      {
        return fileSystemProvider.createDecoratorFileObject(delegate.createData(name, ext)); 
      }

    @Override
    public NwFileObject createFolder (final String name)
        throws IOException
      {
        return fileSystemProvider.createDecoratorFileObject(delegate.createFolder(name));
      }

    @Override @Nonnull
    public String toString() 
      {
        return String.format("DecoratorFileObject(%s)", delegate);
      }
  }    

