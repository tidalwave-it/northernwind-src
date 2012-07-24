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

package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import it.tidalwave.northernwind.core.model.NwFileObject;
import java.io.File;
import lombok.Delegate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openide.filesystems.FileUtil;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor @Slf4j
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
      }
    
    @Inject 
    private ApplicationContext applicationContext;
    
    @Delegate(excludes=Exclusions.class)
    private final org.openide.filesystems.FileObject delegate;

    @Override
    public NwFileObject getParent() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public NwFileObject getFileObject(String fileName) 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public NwFileObject[] getChildren() 
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    @Override
    public Enumeration<? extends NwFileObject> getChildren(boolean b) 
      {
        throw new UnsupportedOperationException("Not supported yet.");
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
    
    // TODO: equals and hashcode
  }
