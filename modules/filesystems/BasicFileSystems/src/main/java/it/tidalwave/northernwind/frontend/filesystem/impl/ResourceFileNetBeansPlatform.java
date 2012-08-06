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
package it.tidalwave.northernwind.frontend.filesystem.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.joda.time.DateTime;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.ResourceFile;
import it.tidalwave.northernwind.core.model.ResourceFileSystem;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j @ToString(of="delegate")
public class ResourceFileNetBeansPlatform implements ResourceFile
  {
    interface Exclusions
      {
        public String getName();
        public ResourceFile getParent();
        public ResourceFile getFileObject (String fileName);
        public Collection<ResourceFile> getChildren();
        public Collection<ResourceFile> getChildren (boolean b);
        public String getMIMEType();
        public File toFile();
        public ResourceFile createFolder (String name);
        public void copyTo (ResourceFile targetFolder);
        public ResourceFileSystem getFileSystem();
        public Date lastModified();
      }
    
    @Inject 
    private ApplicationContext applicationContext;
    
    @Getter @Nonnull
    private final ResourceFileSystemNetBeansPlatform fileSystem;

    @Delegate(excludes=Exclusions.class) @Nonnull
    private final org.openide.filesystems.FileObject delegate;
    
    @Override @Nonnull
    public String getName()
      {
        return delegate.getNameExt();
      }
    
    @Override
    public ResourceFile getParent() 
      {
        return fileSystem.createResourceFile(delegate.getParent());
      }

    @Override
    public ResourceFile getChildByName (final @Nonnull String fileName) 
      {
        return fileSystem.createResourceFile(delegate.getFileObject(fileName));
      }
    
    @Override @Nonnull
    public ResourceFile createFolder (@Nonnull String name)
      throws IOException
      {
        return fileSystem.createResourceFile(delegate.createFolder(name));  
      }

    @Override
    public Collection<ResourceFile> getChildren() 
      {
        final FileObject[] delegateChildren = delegate.getChildren();
        final ResourceFile[] children = new ResourceFile[delegateChildren.length];
        
        for (int i = 0; i < delegateChildren.length; i++)
          {
            children[i] = fileSystem.createResourceFile(delegateChildren[i]);
          }
        
        return Arrays.asList(children);
      }

    @Override
    public Collection<ResourceFile> getChildren (final boolean recursive) 
      {
        final List<ResourceFile> result = new ArrayList<>();
        
        for (final FileObject child : Collections.list(delegate.getChildren(recursive)))
          {
            result.add(fileSystem.createResourceFile(child));  
          }

        return result;
      }
    
    @Override @Nonnull
    public String getMimeType()
      {
        final String fileName = delegate.getNameExt();
        String mimeType = applicationContext.getBean(ServletContext.class).getMimeType(fileName);
        
        if (mimeType == null)
          {
            mimeType = "content/unknown";  
          }
        
        log.trace(">>>> MIME type for {} is {}", fileName, mimeType);
        return mimeType;
      }
    
    @Override @Nonnull
    public DateTime getLatestModificationTime() 
      {
        return new DateTime(delegate.lastModified());
      }
    
    @Override @Nonnull
    public File toFile()
      {  
        return FileUtil.toFile(delegate);
      }
    
    @Override
    public void copyTo (final @Nonnull ResourceFile targetFolder)
      throws IOException 
      {
        FileUtil.copyFile(delegate, ((ResourceFileNetBeansPlatform)targetFolder).delegate, delegate.getName());
      }
    // TODO: equals and hashcode
  }
