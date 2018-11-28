/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2018 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
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
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.experimental.Delegate;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ResourceProperties}.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
// FIXME: this is a patched copy, needs public constructor for builder - see NW-180
@Slf4j @ToString(exclude={"propertyResolver", "asSupport"})
public class DefaultResourceProperties implements ResourceProperties
  {
    @Nonnull @Getter
    private final Id id;

    private final Map<Key<?>, Object> propertyMap = new HashMap<>();

    private final Map<Id, DefaultResourceProperties> groupMap = new HashMap<>();

    @Nonnull
    private final PropertyResolver propertyResolver;

    @Delegate
    private final AsSupport asSupport = new AsSupport(this);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull ResourceProperties.Builder builder)
      {
        this.id = builder.getId();
        this.propertyResolver = builder.getPropertyResolver();
        this.propertyMap.putAll(builder.getValues());
//        for (final Entry<Key<?>, Object> entry : builder.getValues().entrySet())
//          {
//            final String s = entry.getKey().stringValue();
//            final Object value = entry.getValue();
//            propertyMap.put(new Key<>(s), value);
//          }
      }

    /*******************************************************************************************************************
     *
     * Deep clone constructor.
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull DefaultResourceProperties otherProperties)
      {
        this.id = otherProperties.id;
        this.propertyResolver = otherProperties.propertyResolver;

        for (final Entry<Key<?>, Object> entry : otherProperties.propertyMap.entrySet())
          {
            propertyMap.put(entry.getKey(), entry.getValue()); // FIXME: clone the property
          }

        for (final Entry<Id, DefaultResourceProperties> entry : otherProperties.groupMap.entrySet())
          {
            final Id groupId = entry.getKey();
            final DefaultResourceProperties propertyGroup = new DefaultResourceProperties(entry.getValue());
            groupMap.put(groupId, propertyGroup);
          }
      }

    /*******************************************************************************************************************
     *
     * Legacy code for converting from flat-style properties. This is different than passing from() in the Builder,
     * since that approach doesn't support nested groups.
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (final @Nonnull Id id,
                                      final @Nonnull Map<Key<?>, Object> map,
                                      final @Nonnull PropertyResolver propertyResolver)
      {
        this.id = id;
        this.propertyResolver = propertyResolver;

        final Map<Id, Map<Key<?>, Object>> othersMap = new HashMap<>();

        for (final Entry<Key<?>, Object> entry : map.entrySet())
          {
            final String s = entry.getKey().stringValue();
            final Object value = entry.getValue();

            if (!s.contains("."))
              {
                propertyMap.put(new Key<>(s), value);
              }
            else
              {
                final String[] x = s.split("\\.");
                final Id groupId = new Id(x[0]);

                Map<Key<?>, Object> otherMap = othersMap.get(groupId);

                if (otherMap == null)
                  {
                    otherMap = new HashMap<>();
                    othersMap.put(groupId, otherMap);
                  }

                otherMap.put(new Key<>(x[1]), value);
              }
          }

        for (final Entry<Id, Map<Key<?>, Object>> entry : othersMap.entrySet())
          {
            groupMap.put(entry.getKey(), new DefaultResourceProperties(entry.getKey(), entry.getValue(), propertyResolver));
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty (@Nonnull Key<T> key)
      {
        try
          {
            return Optional.of(getProperty2(key));
          }
        catch (NotFoundException | IOException e)
          {
            log.info("", e);
            return Optional.empty();
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull @SuppressWarnings("unchecked")
    public <Type> Type getProperty2 (@Nonnull Key<Type> key)
      throws NotFoundException, IOException
      {
        final Type value = (Type)propertyMap.get(key);
        return (value != null) ? value : propertyResolver.resolveProperty(id, key);
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <Type> Type getProperty2 (final @Nonnull Key<Type> key, final @Nonnull Type defaultValue)
      throws IOException
      {
        try
          {
            return getProperty2(key);
          }
        catch (NotFoundException e)
          {
            return defaultValue;
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public int getIntProperty (final @Nonnull Key<String> key, final int defaultValue)
      throws IOException
      {
        return Integer.parseInt(getProperty2(key, "" + defaultValue));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public boolean getBooleanProperty (final @Nonnull Key<String> key, final boolean defaultValue)
      throws IOException
      {
        return Boolean.parseBoolean(getProperty2(key, "" + defaultValue));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ZonedDateTime getDateTimeProperty (final @Nonnull Collection<Key<String>> keys,
                                              final @Nonnull ZonedDateTime defaultValue)
      {
        for (final Key<String> key : keys)
          {
            try
              {
                return ZonedDateTime.parse(getProperty2(key), DateTimeFormatter.ISO_ZONED_DATE_TIME);
              }
            catch (NotFoundException e)
              {
              }
            catch (IOException e)
              {
                log.warn("", e);
              }
          }

        return defaultValue;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getGroup (final @Nonnull Id id)
      {
        final DefaultResourceProperties properties = groupMap.get(id);
        return properties != null ? properties : new DefaultResourceProperties(this);
//                                  : new DefaultResourceProperties(new Builder().withId(id).withPropertyResolver(propertyResolver));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Collection<Key<?>> getKeys()
      {
        return new CopyOnWriteArrayList<>(propertyMap.keySet());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Collection<Id> getGroupIds()
      {
        return new CopyOnWriteArrayList<>(groupMap.keySet());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> DefaultResourceProperties withProperty (final @Nonnull Key<T> key, final @Nonnull T value)
      {
        final DefaultResourceProperties result = new DefaultResourceProperties(this);
        result.propertyMap.put(key, value); // FIXME: clone property
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultResourceProperties withoutProperty (final @Nonnull Key<?> key)
      {
        final DefaultResourceProperties result = new DefaultResourceProperties(this);
        result.propertyMap.remove(key);
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultResourceProperties withProperties (final @Nonnull ResourceProperties properties)
      {
        final DefaultResourceProperties result = new DefaultResourceProperties(this);
        result.groupMap.put(properties.getId(), new DefaultResourceProperties((DefaultResourceProperties)properties));
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties merged (@Nonnull ResourceProperties properties)
      {
        final DefaultResourceProperties otherProperties = (DefaultResourceProperties)properties;

        if (!id.equals(otherProperties.id))
          {
            throw new IllegalArgumentException("Id mismatch " + id + " vs " + otherProperties.id);
          }

        ResourceProperties result = new DefaultResourceProperties(this);

        for (final Entry<Key<?>, Object> entry : otherProperties.propertyMap.entrySet())
          {
            result = result.withProperty((Key<Object>)entry.getKey(), entry.getValue());
          }

        for (final Entry<Id, DefaultResourceProperties> entry : otherProperties.groupMap.entrySet())
          {
            final Id groupId = entry.getKey();
            final ResourceProperties propertyGroup = entry.getValue();
            result = (!groupMap.containsKey(groupId)) ? result.withProperties(propertyGroup)
                                                      : result.withProperties(groupMap.get(groupId).merged(propertyGroup));
          }

        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties withId (final @Nonnull Id id)
      {
        return new DefaultResourceProperties(this);
//        return new DefaultResourceProperties(new Builder().withId(id).withPropertyResolver(propertyResolver));
      }
  }
