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
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.northernwind.core.impl.model;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.util.spi.AsSupport;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.experimental.Delegate;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import static java.util.Arrays.asList;
import static java.time.format.DateTimeFormatter.*;

/***********************************************************************************************************************
 *
 * The default implementation of {@link ResourceProperties}.
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
// FIXME: this is a patched copy, needs public constructor for builder - see NW-180
@Slf4j @ToString(exclude={"propertyResolver", "asSupport"})
public class DefaultResourceProperties implements ResourceProperties
  {
    private static final Map<Class<?>, Function<Object, Object>> CONVERTER_MAP = new HashMap<Class<?>, Function<Object, Object>>()
      {{
        put(Integer.class,       o -> Integer.parseInt((String)o));
        put(Float.class,         o -> Float.parseFloat((String)o));
        put(Double.class,        o -> Double.parseDouble((String)o));
        put(Boolean.class,       o -> Boolean.parseBoolean((String)o));
        put(ZonedDateTime.class, o -> ZonedDateTime.parse((String)o, ISO_ZONED_DATE_TIME));
      }};

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
//            propertyMap.put(new Key<>(s) {}, value);
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

        otherProperties.propertyMap.entrySet().stream().forEach(e ->
                propertyMap.put(e.getKey(), e.getValue())); // FIXME: clone the property
        otherProperties.groupMap.entrySet().stream().forEach(e ->
                groupMap.put(e.getKey(), new DefaultResourceProperties(e.getValue())));
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
                propertyMap.put(new Key<String>(s) {}, value);
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

                otherMap.put(new Key<String>(x[1]) {}, value);
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
            final Object value = propertyMap.get(key);
            return Optional.of(convertValue(key, (value != null) ? value : propertyResolver.resolveProperty(id, key)));
          }
        catch (NotFoundException | IOException e)
          {
            log.trace("", e);
            return Optional.empty();
          }
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

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    /* visible for testing */ static <T> T convertValue (final @Nonnull Key<T> key, final @Nonnull Object value)
      {
        try
          {
            if (key.toString().equals("Key[tags]")) // workaround as Zephyr stores it as a comma-separated string
              {
                return (T)asList(((String)value).split(","));
              }
//            if (value instanceof List)
//              {
//                final List<Object> list = (List<Object>)value;
//                Class<?> elementType = String.class; // FIXME: should get the generic of the list
//
//                return (T)list.stream()
//                              .map(i -> CONVERTER_MAP.getOrDefault(elementType, o -> o).apply(value))
//                              .collect(toList());
//              }
//            else
              {
                return (T)CONVERTER_MAP.getOrDefault(key.getType(), o -> o).apply(value);
              }
          }
        catch (Exception e)
          {
            throw new RuntimeException(String.format("Can't convert '%s' to %s(%s)", value, key, key.getType()), e);
          }
      }
  }
