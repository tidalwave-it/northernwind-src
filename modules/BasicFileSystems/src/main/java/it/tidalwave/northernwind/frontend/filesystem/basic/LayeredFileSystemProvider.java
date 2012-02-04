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
package it.tidalwave.northernwind.frontend.filesystem.basic;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import it.tidalwave.northernwind.core.filesystem.FileSystemProvider;
import java.util.Map;
import lombok.Delegate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class LayeredFileSystemProvider implements FileSystemProvider
  {
    @Getter @Setter @Nonnull
    private List<? extends FileSystemProvider> delegates = new ArrayList<FileSystemProvider>();
    
    @Getter @Setter
    private FileSystemProvidersProvider fileSystemProvidersProvider;
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    static interface FileDelegateExclusions
      {
        public void delete();
        
        public boolean hasExt (String ext);
        
        public OutputStream getOutputStream();
        
        public boolean canRevert();     
        
        public void revert();
        
        public URL getURL();
        
        public URL toURL();
        
        public URI toURI();

        public FileSystem getFileSystem();
        
        public FileObject getParent();
        
        public FileObject move (FileLock lock, FileObject target, String name, String ext);
        
        public FileObject copy (FileObject target, String name, String ext);
        
        public FileObject createData (String name, String ext);
        
        public FileObject createFolder (String name);
        
//        public Enumeration<? extends FileObject> getData (boolean rec); calls getChildren()
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    abstract class FileObjectDelegateSupport extends FileObject
      {
        @Override @Nonnull
        public FileSystem getFileSystem()
          {
            return fileSystem;
          }

        @Override
        public FileObject move (FileLock lock, FileObject target, String name, String ext)
          throws IOException
          {
            return createDecoratorFileObject(super.move(lock, target, name, ext));
          }
        
        @Override
        public FileObject copy (FileObject target, String name, String ext)
          throws IOException
          {
            return createDecoratorFileObject(super.copy(target, name, ext));
          }
        
        @Override
        public boolean equals (final Object object)
          {
              log.info("EQUALS? {}, {}", this, object);
            if ((object == null) || (getClass() != object.getClass()))
              {
                return false;
              }
            
            final FileObjectDelegateSupport other = (FileObjectDelegateSupport)object;
            return (this.getFileSystem() == other.getFileSystem()) && this.getPath().equals(other.getPath());
          }

        @Override
        public int hashCode()
          {
            return getFileSystem().hashCode() ^ getPath().hashCode();
          }
      }
     
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    class DecoratorFileObject extends FileObjectDelegateSupport
      {
        @Delegate(types=FileObject.class, excludes=FileDelegateExclusions.class) @Nonnull
        private final FileObject delegate;
        
        @Override
        public FileObject getParent()
          {
            return createDecoratorFileObject(delegate.getParent());
          }
        
        @Override
        public FileObject createData (String name, String ext)
          throws IOException
          {
            return createDecoratorFileObject(delegate.createData(name, ext)); 
          }
        
        @Override
        public FileObject createFolder (String name)
          throws IOException
          {
            return createDecoratorFileObject(delegate.createFolder(name));
          }
        
        @Override @Nonnull
        public String toString() 
          {
            return String.format("DecoratorFileObject(%s)", delegate);
          }
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    static interface FolderDelegateExclusions extends FileDelegateExclusions
      {
        @Nonnull
        public FileObject[] getChildren();
        
        public FileObject getFileObject (String relativePath);
        
        public FileObject getFileObject (String name, String ext);
        
//        public Enumeration<? extends FileObject> getFolders (boolean rec); calls getChildren()
      }
        
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor
    class DecoratorFolderObject extends FileObjectDelegateSupport
      {
        @Nonnull
        private final String path;
        
        @Delegate(types=FileObject.class, excludes=FolderDelegateExclusions.class) @Nonnull
        private final FileObject delegate;

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public FileObject[] getChildren() 
          {
            log.trace("getChildren() - {}", this);
            return getChildrenMap().values().toArray(new FileObject[0]);
          }
        
        @Override
        public FileObject getFileObject (String relativePath)
          {
            log.trace("getFileObject({})", relativePath);
            
            if (relativePath.contains("/"))
              {
                throw new IllegalArgumentException("relativePath: " + relativePath);  
              }
            
            return getChildrenMap().get(relativePath);
          }
        
        @Override
        public FileObject getFileObject (String name, String ext)
          {
            log.trace("getFileObject({}, {})", name, ext);
            return getFileObject(name + "." + ext);
//            return createDecoratorFileObject(delegate.getFileObject(name, ext)); 
          }
        
        @Override
        public FileObject getParent()
          {
            return createDecoratorFileObject(delegate.getParent());
          }
        
        @Override
        public FileObject createData (String name, String ext)
          throws IOException
          {
            log.trace("createData({}, {})", name, ext);
            return createDecoratorFileObject(delegate.createData(name, ext)); 
          }
        
        @Override
        public FileObject createFolder (String name)
          throws IOException
          {
            log.trace("createFolder({})", name);
            return createDecoratorFileObject(delegate.createFolder(name));
          }
        
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String toString() 
          {
            return String.format("DecoratorFolderObject(%s)", delegate);
          }
        
        /***************************************************************************************************************
         *
         * 
         *
         **************************************************************************************************************/
        @Nonnull
        private Map<String, FileObject> getChildrenMap()
          {
            final SortedMap<String, FileObject> childrenMap = new TreeMap<String, FileObject>();
            
            for (final ListIterator<? extends FileSystemProvider> i = delegates.listIterator(delegates.size()); i.hasPrevious(); )
              {
                try
                  {
                    final FileSystem fileSystem = i.previous().getFileSystem();
                    final FileObject delegateDirectory = fileSystem.findResource(path);

                    if (delegateDirectory != null)
                      {
                        for (final FileObject fileObject : delegateDirectory.getChildren())
                          {
                            if (!childrenMap.containsKey(fileObject.getNameExt()))
                              {
                                childrenMap.put(fileObject.getNameExt(), createDecoratorFileObject(fileObject));
                              }
                          }
                      }
                  } 
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }
            
            log.trace(">>>> childrenMap: {}", childrenMap);
            return childrenMap;
          }
      }
     
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private final FileSystem fileSystem = new FileSystem() 
      {
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public String getDisplayName() 
          {
            return "LayeredFileSystem";
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override
        public boolean isReadOnly()
          {
            return true;
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public FileObject getRoot() 
          {
            return findResource("");
          }

        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public FileObject findResource (final @Nonnull String name) 
          {
            log.trace("findResource({})", name);
            FileObject result = null;
            
              // FIXME: move to init!
            if (fileSystemProvidersProvider != null)
              {
                delegates.clear();
                final List fileSystemProviders = fileSystemProvidersProvider.getFileSystemProviders();
                delegates.addAll(fileSystemProviders);
              }
            
            for (final ListIterator<? extends FileSystemProvider> i = delegates.listIterator(delegates.size()); i.hasPrevious(); )
              {
                try 
                  {
                    final FileSystem fileSystem = i.previous().getFileSystem();
                    final FileObject fileObject = fileSystem.findResource(name);

                    if (fileObject != null)
                      {
                        log.trace(">>>> fileSystem: {}, fileObject: {}", fileSystem, fileObject.getPath());
                        result = createDecoratorFileObject(fileObject);
                        break;
                      }
                  } 
                catch (IOException e)
                  {
                    log.warn("", e);
                  }
              }

            log.trace(">>>> returning {}", result);
            
            return result;
          }
        
        /***************************************************************************************************************
         *
         * {@inheritDoc}
         *
         **************************************************************************************************************/
        @Override @Nonnull
        public SystemAction[] getActions()
          {
            try 
              {
                return delegates.get(delegates.size() - 1).getFileSystem().getActions();
              } 
            catch (IOException e)
              {
                throw new RuntimeException(e);
              }
          }
      };
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public FileSystem getFileSystem()  
      {
        return fileSystem;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private FileObject createDecoratorFileObject (final @Nonnull FileObject delegate)
      {
        return (delegate == null) ? null
                                  : (delegate.isData() ? new DecoratorFileObject(delegate) 
                                                       : new DecoratorFolderObject(delegate.getPath(), delegate));  
      }
  }
