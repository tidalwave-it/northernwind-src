/***********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * Copyright (C) 2011-2011 by Tidalwave s.a.s. (http://www.tidalwave.it)
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
 * SCM: http://java.net/hg/northernwind~src
 *
 **********************************************************************************************************************/
package it.tidalwave.northernwind.importer.infoglue;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class Main 
  {
    /* package */ static final Map<String, String> assetFileNameMapByKey = new HashMap<String, String>();
    
    /* package */ static final ContentMap contentMap = new ContentMap();
    
    // blueBill Mobile
//    /* package */ static String xmlFile = "/home/fritz/Business/Tidalwave/Projects/WorkAreas/blueBill/Export__blueBill_2011-08-10_0414.xml";
//    /* package */ static String zipLibraryFile = "/home/fritz/Business/Tidalwave/Projects/WorkAreas/blueBill/blueBillWebsiteLibrary.zip";
//    /* package */ static public String contentPrefix = "^/blueBill/Mobile";
//    /* package */ static public String siteNodePrefix = "^/blueBill/Mobile";
    
    // StoppingDown
    /* package */ static String xmlFile = "/home/fritz/Personal/WebSites/Export__stoppingdown.net_2011-08-11_0536.xml";
    /* package */ static String zipLibraryFile = "/home/fritz/Personal/WebSites/StoppingDownWebsiteLibrary.zip";
    /* package */ static String contentPrefix = "^/Stopping Down";
    /* package */ static String siteNodePrefix = "^/stoppingdown\\.net";
                    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void main (final @Nonnull String ... args)
      throws Exception
      {
        new ClassPathXmlApplicationContext
          (
            "classpath*:/META-INF/*AutoBeans.xml",
            "classpath*:/META-INF/StandAloneConfigurationBeans.xml",
            "classpath*:/META-INF/SimpleLocalFileSystemBeans.xml"
          );
        
        new ExportConverter(new FileInputStream(xmlFile)).process() ;
      }
  }
