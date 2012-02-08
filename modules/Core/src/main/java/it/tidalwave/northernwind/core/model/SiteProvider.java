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
package it.tidalwave.northernwind.core.model;

import javax.annotation.Nonnull;
import java.io.IOException;
import it.tidalwave.util.NotFoundException;

/***********************************************************************************************************************
 *
 * @author  fritz
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface SiteProvider 
  {
    public static final Class<SiteProvider> SiteProvider = SiteProvider.class;
    
    @Nonnull
    public Site getSite()
      throws NotFoundException;

    public void reset() // FIXME: rename to reload()
      throws NotFoundException, IOException;
    
    /*******************************************************************************************************************
     *
     * Returns the version string of NorthernWind.
     * 
     * @return   the version
     *
     ******************************************************************************************************************/
    @Nonnull
    public String getVersionString();
  }
