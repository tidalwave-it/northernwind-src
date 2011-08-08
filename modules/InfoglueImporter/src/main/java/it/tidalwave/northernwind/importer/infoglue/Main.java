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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class Main 
  {
    private static ApplicationContext applicationContext;
        
    public static final File hgFolder = new File("target/root");      
    
    /* package */ static final Map<String, String> assetFileNameMapByKey = new HashMap<String, String>();
    
    /* package */ static final ContentMap contentMap = new ContentMap();
    
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void main (String[] args)
      throws Exception
      {
        applicationContext = new ClassPathXmlApplicationContext(
                "classpath*:/META-INF/*AutoBeans.xml",
                "classpath*:/META-INF/StandAloneConfigurationBeans.xml",
                "classpath*:/META-INF/SimpleLocalFileSystemBeans.xml");
        
        hgFolder.mkdirs();
        Utilities.exec("/bin/sh", "-c", "cd " + hgFolder.getAbsolutePath() + " && /usr/bin/hg init");
        
        new ExportConverter(new FileInputStream(System.getProperty("user.home") + "/Downloads/Export__blueBill_2011-07-17_1747.xml")).process() ;
        ResourceManager.addAndCommitResources();
        Utilities.exec("/bin/sh", "-c", "cd " + Main.hgFolder.getAbsolutePath() + " && /usr/bin/hg tag converted");
      }
  }
