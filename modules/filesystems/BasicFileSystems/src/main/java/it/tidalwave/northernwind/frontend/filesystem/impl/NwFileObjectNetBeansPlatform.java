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
 * WWW: http://northernwind.tidalwave.it
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/

package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.NwFileObject;
import it.tidalwave.northernwind.core.model.NwFileSystem;
import java.io.File;
import java.io.IOException;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j @ToString(of="delegate")
public class NwFileObjectNetBeansPlatform implements NwFileObject 
  {
    interface Exclusions
      {
        public NwFileObject getParent();
        public NwFileObject getFileObject (String fileName);
        public NwFileObject[] getChildren();
        public Enumeration<? extends NwFileObject> getChildren (boolean b);
        public String getMIMEType();
        public File toFile();
        public NwFileObject createFolder (String name);
        public void copyTo (NwFileObject targetFolder);
        public NwFileSystem getFileSystem();
      }
    
    @Inject 
    private ApplicationContext applicationContext;
    
    @Getter @Nonnull
    private final NwFileSystemNetBeansPlatform fileSystem;

    @Delegate(excludes=Exclusions.class) @Nonnull
    private final org.openide.filesystems.FileObject delegate;
    
    @Override
    public NwFileObject getParent() 
      {
        return fileSystem.createNwFileObject(delegate.getParent());
      }

    @Override
    public NwFileObject getFileObject (final @Nonnull String fileName) 
      {
        return fileSystem.createNwFileObject(delegate.getFileObject(fileName));
      }

    @Override
    public NwFileObject[] getChildren() 
      {
        final FileObject[] delegateChildren = delegate.getChildren();
        final NwFileObject[] children = new NwFileObject[delegateChildren.length];
        
        for (int i = 0; i < delegateChildren.length; i++)
          {
            children[i] = fileSystem.createNwFileObject(delegateChildren[i]);
          }
        
        return children;
      }

    @Override
    public Enumeration<? extends NwFileObject> getChildren (final boolean recursive) 
      {
        final Enumeration<? extends FileObject> children = delegate.getChildren(recursive);
        
        return new Enumeration<NwFileObject>()
          {
            @Override
            public boolean hasMoreElements() 
              {
                return children.hasMoreElements();
              }

            @Override @Nonnull
            public NwFileObject nextElement() 
              {
                return fileSystem.createNwFileObject(children.nextElement());
              }
        };
      }
    
    @Override @Nonnull
    public String getMIMEType()
      {
        final String fileName = delegate.getNameExt();
        final String mimeType = applicationContext.getBean(ServletContext.class).getMimeType(fileName);
        log.trace(">>>> MIME type for {} is {}", delegate, mimeType);
        return mimeType;
      }
    
    @Override @Nonnull
    public File toFile()
      {  
        return FileUtil.toFile(delegate);
      }
    
    @Override @Nonnull
    public NwFileObject createFolder (@Nonnull String name)
      throws IOException
      {
        return fileSystem.createNwFileObject(delegate.createFolder(name));  
      }
    
    @Override
    public void copyTo (final @Nonnull NwFileObject targetFolder)
      throws IOException 
      {
        FileUtil.copyFile(delegate, ((NwFileObjectNetBeansPlatform)targetFolder).delegate, delegate.getName());
      }
    // TODO: equals and hashcode
  }
