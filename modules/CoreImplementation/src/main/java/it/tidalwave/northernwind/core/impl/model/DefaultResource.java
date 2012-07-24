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
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import it.tidalwave.northernwind.core.model.NwFileObject;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.RequestLocaleManager;
import it.tidalwave.northernwind.core.model.Resource;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Cleanup;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Unmarshallable.Unmarshallable;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @Slf4j @ToString(exclude={"localeRequestManager", "macroExpander", "properties", "propertyResolver"})
/* package */ class DefaultResource implements Resource
  {
    @Inject @Nonnull
    private RequestLocaleManager localeRequestManager;
    
    @Inject @Nonnull
    private Provider<FilterSetExpander> macroExpander;
    
    @Nonnull @Getter
    private final NwFileObject file;    
    
    @Nonnull @Getter
    private ResourceProperties properties;
    
    @Getter
    private boolean placeHolder;
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private DefaultResourceProperties.PropertyResolver propertyResolver = new DefaultResourceProperties.PropertyResolver()
      {
        @Override
        public <Type> Type resolveProperty (final @Nonnull Id propertyGroupId, final @Nonnull Key<Type> key) 
          throws NotFoundException, IOException
          {
            return (Type)getFileBasedProperty(key.stringValue()); // FIXME: use also Id for SiteNode?
          }
      };
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResource (final @Nonnull NwFileObject file) 
      {
        this.file = file;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getPropertyGroup (final @Nonnull Id id)
      {
        return properties.getGroup(id);   
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private String getFileBasedProperty (final @Nonnull String propertyName)
      throws NotFoundException, IOException
      {
        log.trace("getFileBasedProperty({})", propertyName);
        
        final NwFileObject propertyFile = findLocalizedFile(propertyName);
        log.trace(">>>> reading from {}", propertyFile.getPath());
        final String charset = propertyFile.getMIMEType().equals("application/xhtml+xml") ? "UTF-8" : Charset.defaultCharset().name();
        
        try
          {
            return macroExpander.get().filter(propertyFile.asText(charset), propertyFile.getMIMEType());
          }
        catch (RuntimeException e) // FIXME: introduce a FilterException
          {
            throw new IOException(e); 
          }
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
        boolean tmpPlaceHolder = true;
                
        properties = new DefaultResourceProperties(new Id(""), propertyResolver);

        for (final NwFileObject propertyFile : Utilities.getInheritedPropertyFiles(file, "Properties_en.xml"))
          {
            log.trace(">>>> reading properties from /{}...", propertyFile.getPath());
            @Cleanup final InputStream is = propertyFile.getInputStream();
            final ResourceProperties tempProperties = new DefaultResourceProperties(propertyResolver).as(Unmarshallable).unmarshal(is);
            log.trace(">>>>>>>> read properties: {}", tempProperties);
            properties = properties.merged(tempProperties);
            tmpPlaceHolder &= !propertyFile.getParent().equals(file);
          }
        
        placeHolder = Boolean.parseBoolean(properties.getProperty(PROPERTY_PLACE_HOLDER, "" + tmpPlaceHolder));

        if (log.isDebugEnabled())
          {
            log.debug(">>>> properties for /{}:", file.getPath());
            logProperties(">>>>>>>>", properties);
          }
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private NwFileObject findLocalizedFile (final @Nonnull String fileName)
      throws NotFoundException
      {
        log.trace("findLocalizedFile({})", fileName);
        NwFileObject localizedFile = null;
        final StringBuilder fileNamesNotFound = new StringBuilder();
        String separator = "";
        
        for (final Locale locale : localeRequestManager.getLocales())
          {
            final String localizedFileName = fileName.replace(".", "_" + locale.getLanguage() + ".");
            localizedFile = file.getFileObject(localizedFileName);
            
            if ((localizedFile == null) && localizedFileName.endsWith(".xhtml"))
              {
                localizedFile = file.getFileObject(localizedFileName.replaceAll("\\.xhtml$", ".html"));
              }
            
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
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    private void logProperties (final @Nonnull String indent ,final @Nonnull ResourceProperties properties)
      {
        log.debug("{} simple property items:", indent);
        
        for (final Key<?> key : properties.getKeys())
          {
            try 
              {
                log.debug("{}>>>> {} = {}", new Object[] { indent, key, properties.getProperty(key) });
              }
            catch (NotFoundException e) 
              {
                log.error("", e);
              }
            catch (IOException e) 
              {
                log.error("", e);
              }
          }
        
        log.debug("{} property groups: {}", indent, properties.getGroupIds());
        
        for (final Id groupId : properties.getGroupIds())
          {
            log.debug("{}>>>> group: {}", indent, groupId);
            logProperties(indent + ">>>>", properties.getGroup(groupId));
          }
      }
  }
