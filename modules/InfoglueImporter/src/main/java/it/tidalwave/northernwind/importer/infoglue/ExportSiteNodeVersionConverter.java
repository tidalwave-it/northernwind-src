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

import it.tidalwave.northernwind.core.impl.util.UriUtilities;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamReader;
import lombok.Getter;
import org.joda.time.DateTime;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
class ExportSiteNodeVersionConverter extends Converter
  {
    private int stateId;

    @Getter 
    private DateTime modifiedDateTime;
 
    @Getter 
    private String versionComment;
    
    private boolean checkedOut;
    
    private boolean active;
    
    @Getter 
    private String versionModifier;
    
    private String contentType;
    
    private final ExportSiteNodeConverter parent;
    
    private int metaInfoContentId;
    
    // TODO: bindingQualifyers.name & value (contentId) & sortOrder
    
    public ExportSiteNodeVersionConverter (final @Nonnull ExportSiteNodeConverter parent)
      {
        super(parent);        
        this.parent = parent;
      }

    @Override
    protected void processStartElement (final @Nonnull String elementName, final @Nonnull XMLStreamReader reader)
      throws Exception
      {
      }
    
    @Override
    protected void processEndElement (final @Nonnull String elementName)
      throws Exception
      {
        if ("stateId".equals(elementName))
          {
            stateId = contentAsInteger();  
          }
        else if ("modifiedDateTime".equals(elementName))
          {
            modifiedDateTime = contentAsDateTime();  
          }
        else if ("versionComment".equals(elementName))
          {
            versionComment = contentAsString();  
          }
        else if ("isCheckedOut".equals(elementName))
          {
            checkedOut = contentAsBoolean();  
          }
        else if ("isActive".equals(elementName))
          {
            active = contentAsBoolean();  
          }
        else if ("versionModifier".equals(elementName))
          {
            versionModifier = contentAsString();  
          }
        else if ("contentType".equals(elementName))
          {
            contentType = contentAsString();  
          }
        else if ("value".equals(elementName)) // FIXME: it's in a inner element
          {
            metaInfoContentId = contentAsInteger();  
          }
      }

    @Override
    protected void finish()
      throws Exception 
      {
        String path = parent.getPath();
        log.trace("Now processing {} stateId: {}, checkedOut: {}, active: {}, {} {}", 
                   new Object[] { path, stateId, checkedOut, active, modifiedDateTime, versionComment });
        
        if (path.equals("/blueBill/RSS Feeds/Mobile News"))
          {
            path = "/blueBill/Mobile/Blog RSS Feed";  
          }
//        else if (path.equals("/blueBill/RSS Feeds"))
//          {
//            path = "/blueBill/Mobile/RSS Feeds";  
//          }
      
//        if (path.startsWith("/blueBill/Mobile"))
        if (!path.matches(Main.siteNodePrefix + ".*") || path.contains("/Blog/"))
          {
            log.warn("Ignoring sitenode: {}", path);
          }
        else
          {
            path = path.replaceAll(Main.siteNodePrefix, "");
            log.debug(">>>> replaced path: {}", path);
            final String suffix = path.startsWith("/_Standard Pages") || path.startsWith("/Blog RSS Feed") ? "/" : "/Override";
            path = path.replaceAll("^/_Standard\\ Pages", "");
            path = UriUtilities.urlEncodedPath("/structure" + path) + suffix;
            log.info("Processing {} -> {}", parent.getPath(), path);

            final Map<String, String> languageMap = Main.contentMap.get(metaInfoContentId, modifiedDateTime);
            
            for (final Entry<String, String> entry : languageMap.entrySet())
              {
                final String languageCode = entry.getKey();
                final String content = entry.getValue();

                if (content == null)
                  {
                    log.error("No content for " + "" + metaInfoContentId + "/" + modifiedDateTime);  
                    continue;
                  }

                // FIXME: creationDateTime, comment
                new StructureParser(content, modifiedDateTime, parent.getPublishDateTime(), path, languageCode).process(); 
              }
          }
      }

    @Override @Nonnull
    public String toString() 
      {
        return String.format("ExportSiteNodeVersionConverter(%s)", parent.getPath());
      }
  }