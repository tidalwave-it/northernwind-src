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
package it.tidalwave.northernwind.frontend.ui.component;

//import org.joda.time.DateTime;
import it.tidalwave.util.Key;
import lombok.NoArgsConstructor;
import static lombok.AccessLevel.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access=PRIVATE)
public final class Properties 
  {
    public static final Key<String> PROPERTY_TITLE = new Key<String>("title");
    
    public static final Key<String> PROPERTY_DESCRIPTION = new Key<String>("description");
    
    public static final Key<String> PROPERTY_FULL_TEXT = new Key<String>("fullText.xhtml");
    
    public static final Key<String> PROPERTY_TEMPLATE = new Key<String>("template.xhtml");
    // FIXME: those should be Key<DateTime>
    public static final Key<String> PROPERTY_CREATION_DATE = new Key<String>("creationDateTime"); 
    
    public static final Key<String> PROPERTY_PUBLISHING_DATE = new Key<String>("publishingDateTime");   
    
    public static final Key<String> PROPERTY_LATEST_MODIFICATION_DATE = new Key<String>("latestModificationDateTime");    
    
    public static final Key<String> PROPERTY_CLASS = new Key<String>("class");
    
    public static final Key<String> PROPERTY_TEMPLATE_RESOURCE = new Key<String>("template");
    
    public static final Key<String> PROPERTY_WRAPPER_TEMPLATE_RESOURCE = new Key<String>("wrapperTemplate");
    
    public static final Key<String> PROPERTY_DATE_FORMAT = new Key<String>("dateFormat");
    
  }
