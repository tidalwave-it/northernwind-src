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
package it.tidalwave.northernwind.frontend.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import org.openide.filesystems.FileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.frontend.model.RequestLocaleManager;
import it.tidalwave.northernwind.frontend.model.Resource;
import it.tidalwave.northernwind.frontend.model.ResourceProperties;
import it.tidalwave.northernwind.frontend.model.Site;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(exclude={"site", "localeRequestManager", "properties", "propertyResolver"})
/* package */ class DefaultResource implements Resource
  {
    @Inject @Nonnull
    private Site site;
    
    @Inject @Nonnull
    private RequestLocaleManager localeRequestManager;
    
    @Nonnull @Getter
    private final FileObject file;    
    
    @Nonnull
    private ResourceProperties properties;
    
    private DefaultResourceProperties.PropertyResolver propertyResolver = new DefaultResourceProperties.PropertyResolver()
      {
        @Override
        public <Type> Type resolveProperty (final @Nonnull Id propertyGroupId, final @Nonnull Key<Type> key) 
          throws NotFoundException, IOException
          {
            return (Type)getFileBasedProperty(key.stringValue()); // FIXME: use also Id
          }
      };
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResource (final @Nonnull FileObject file) 
      {
        this.file = file;
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getProperties()
      {
        return properties;   
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getProperties (final @Nonnull Id id)
      throws NotFoundException
      {
        return properties.getGroup(id);   
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getFileBasedProperty (final @Nonnull String propertyName)
      throws NotFoundException, IOException
      {
        log.trace("getFileBasedProperty({})", propertyName);
        
        final FileObject propertyFile = findLocalizedFile(propertyName);
        log.trace(">>>> reading from {}", propertyFile.getPath());
        String text = propertyFile.asText();

//        // FIXME: this should be done in a specific postprocessor registered only for Content   
//        // FIXME: and do this with StringTemplate - remember to escape $'s in the source
//        final String c = site.getContextPath();
//        final STGroup g = new STGroupString("",
//                "mediaLink(relativeUri) ::= " + c + "/media/$relativeUri$\n" +
//                "nodeLink(relativeUri)  ::= " + c + "$relativeUri$\n", '$', '$');
        text = text.replaceAll("\\$mediaLink\\(relativeUri=([^)]*)\\)\\$", site.getContextPath() + "/media/$1");
        text = text.replaceAll("\\$nodeLink\\(relativeUri=([^)]*)\\)\\$", site.getContextPath() + "/$1");
        
        return text;
      }  
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* package */ void loadProperties()
      throws IOException
      {
        log.trace("loadProperties() for /{}", file.getPath());
                
        final Map<Key<?>, Object> map = new HashMap<Key<?>, Object>();

        for (final FileObject propertyFile : Utilities.getInheritedPropertyFiles(file, "Resource_en.properties"))
          {
            log.trace(">>>> reading properties from /{}...", propertyFile.getPath());
            @Cleanup final Reader r = new InputStreamReader(propertyFile.getInputStream());
            final Properties tempProperties = new Properties();
            tempProperties.load(r);
            log.trace(">>>> local properties: {}", tempProperties);
            r.close();        
            
            for (final Entry<Object, Object> entry : tempProperties.entrySet())
              {
                map.put(new Key<Object>(entry.getKey().toString()), entry.getValue());
              }
          }

        properties = new DefaultResourceProperties(new Id(""), map, propertyResolver);
        log.debug(">>>> properties for /{}: {}", file.getPath(), properties);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private FileObject findLocalizedFile (final @Nonnull String fileName)
      throws NotFoundException
      {
        FileObject localizedFile = null;
        final StringBuilder fileNamesNotFound = new StringBuilder();
        String separator = "";
        
        for (final Locale locale : localeRequestManager.getLocales())
          {
            final String localizedFileName = fileName.replace(".", "_" + locale.getLanguage() + ".");
            localizedFile = file.getFileObject(localizedFileName);
            
            if (localizedFile != null)
              {
                break;  
              }
            
            fileNamesNotFound.append(separator);
            fileNamesNotFound.append(localizedFileName);
            separator = ",";
          }

        return NotFoundException.throwWhenNull(localizedFile, String.format("%s/{%s}", file.getPath(), fileNamesNotFound));  
      }
  }
