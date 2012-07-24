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

import java.util.Enumeration;
import it.tidalwave.northernwind.core.model.NwFileObject;
import lombok.Delegate;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor
public class NwFileObjectNetBeansPlatform implements NwFileObject 
  {
    interface Exclusions
      {
        public NwFileObject getParent();
        public NwFileObject getFileObject (String fileName);
        public NwFileObject[] getChildren();
        public Enumeration<? extends NwFileObject> getChildren (boolean b);
      }
    
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
    
    // TODO: equals and hashcode
  }
