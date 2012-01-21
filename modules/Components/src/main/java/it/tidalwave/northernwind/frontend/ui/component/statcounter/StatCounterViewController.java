/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2012 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * WWW: http://northernwind.java.net
 * SCM: https://bitbucket.org/tidalwave/northernwind-src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.frontend.ui.component.statcounter;

import it.tidalwave.util.Key;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface StatCounterViewController
  {  
    public static final Key<String> PROPERTY_PROJECT = new Key<String>("project");
    
    public static final Key<String> PROPERTY_SECURITY = new Key<String>("security");
    
    // FIXME: should be Boolean
    public static final Key<String> PROPERTY_INVISIBLE = new Key<String>("invisible");
    
    public static final Key<String> PROPERTY_MESSAGE = new Key<String>("message");
    
    // FIXME: these should be Integer
    public static final Key<String> PROPERTY_PARTITION = new Key<String>("partition");
    
    public static final Key<String> PROPERTY_CLICK_STAT = new Key<String>("clickStat");
    
    public static final Key<String> PROPERTY_TEXT = new Key<String>("text");
  }
