/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2023 Tidalwave s.a.s. (http://tidalwave.it)
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.io.IOException;
import it.tidalwave.util.As;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.NotFoundException;
import it.tidalwave.northernwind.core.model.ResourcePath;
import it.tidalwave.northernwind.core.model.ResourceProperties;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.util.stream.Collectors.*;

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
    @SuppressWarnings("squid:S1171")
    private static final Map<Class<?>, Function<String, Object>> CONVERTER_MAP =
        new HashMap<>()
          {{
            put(Integer.class,        Integer::parseInt);
            put(Float.class,          Float::parseFloat);
            put(Double.class,         Double::parseDouble);
            put(Boolean.class,        Boolean::parseBoolean);
            put(ZonedDateTime.class,  o -> ZonedDateTime.parse(o, ISO_ZONED_DATE_TIME));
            put(ResourcePath.class,   ResourcePath::of);
          }};

    @Nonnull @Getter
    private final Id id;

    /* Use String as key, and not Key. In this way properties can be managed both in an untyped fashion - e.g. by means
    of Key.of("foo", Object.class) - and typed at the same time - e.g. Key.of("foo", Boolean.class). */
    private final Map<String, Object> propertyMap = new HashMap<>();

    private final Map<Id, DefaultResourceProperties> groupMap = new HashMap<>();

    @Nonnull
    private final PropertyResolver propertyResolver;

    @Delegate
    private final As asSupport = As.forObject(this);

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (@Nonnull final ResourceProperties.Builder builder)
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
    public DefaultResourceProperties (@Nonnull final DefaultResourceProperties otherProperties)
      {
        this.id = otherProperties.id;
        this.propertyResolver = otherProperties.propertyResolver;

        otherProperties.propertyMap.forEach(propertyMap::put); // FIXME: clone the property
        otherProperties.groupMap.forEach((k, v) -> groupMap.put(k, new DefaultResourceProperties(v)));
      }

    /*******************************************************************************************************************
     *
     * Legacy code for converting from flat-style properties. This is different than passing from() in the Builder,
     * since that approach doesn't support nested groups.
     *
     ******************************************************************************************************************/
    public DefaultResourceProperties (@Nonnull final Id id,
                                      @Nonnull final Map<String, Object> map,
                                      @Nonnull final PropertyResolver propertyResolver)
      {
        this.id = id;
        this.propertyResolver = propertyResolver;

        final Map<Id, Map<String, Object>> othersMap = new HashMap<>();

        for (final var entry : map.entrySet())
          {
            final var s = entry.getKey();
            final var value = entry.getValue();

            if (!s.contains("."))
              {
                propertyMap.put(s, value);
              }
            else
              {
                final var x = s.split("\\.");
                final var groupId = new Id(x[0]);
                final var otherMap = othersMap.computeIfAbsent(groupId, __ -> new HashMap<>());
                otherMap.put(x[1], value);
              }
          }

        for (final var entry : othersMap.entrySet())
          {
            groupMap.put(entry.getKey(), new DefaultResourceProperties(entry.getKey(), entry.getValue(), propertyResolver));
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> Optional<T> getProperty (@Nonnull final Key<? extends T> key)
      {
        try
          {
            final var value = propertyMap.get(key.getName());
            return Optional.of(convertValue(key, (value != null) ? value : propertyResolver.resolveProperty(id, key)));
          }
        catch (IOException e)
          {
            log.trace("Could not resolve property", e);
            return Optional.empty();
          }
        catch (NotFoundException e)
          {
            log.trace("Could not resolve property {}", e.getMessage());
            return Optional.empty();
          }
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties getGroup (@Nonnull final Id id)
      {
        final var properties = groupMap.get(id);
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
        return propertyMap.keySet().stream().map(Key::of).collect(toList());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public Collection<Id> getGroupIds() // FIXME: should be a Set
      {
        return new CopyOnWriteArrayList<>(groupMap.keySet());
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public <T> DefaultResourceProperties withProperty (@Nonnull final Key<T> key, @Nonnull final T value)
      {
        final var result = new DefaultResourceProperties(this);
        result.propertyMap.put(key.getName(), value); // FIXME: clone property
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultResourceProperties withoutProperty (@Nonnull final Key<?> key)
      {
        final var result = new DefaultResourceProperties(this);
        result.propertyMap.remove(key.getName());
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public DefaultResourceProperties withProperties (@Nonnull final ResourceProperties properties)
      {
        final var result = new DefaultResourceProperties(this);
        result.groupMap.put(properties.getId(), new DefaultResourceProperties((DefaultResourceProperties)properties));
        return result;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public ResourceProperties merged (@Nonnull final ResourceProperties properties)
      {
        final var otherProperties = (DefaultResourceProperties)properties;

        if (!id.equals(otherProperties.id))
          {
            throw new IllegalArgumentException("Id mismatch " + id + " vs " + otherProperties.id);
          }

        ResourceProperties result = new DefaultResourceProperties(this);

        for (final var entry : otherProperties.propertyMap.entrySet())
          {
            result = result.withProperty(Key.of(entry.getKey()), entry.getValue());
          }

        for (final var entry : otherProperties.groupMap.entrySet())
          {
            final var groupId = entry.getKey();
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
    public ResourceProperties withId (@Nonnull final Id id)
      {
        return new DefaultResourceProperties(this);
//        return new DefaultResourceProperties(new Builder().withId(id).withPropertyResolver(propertyResolver));
      }

    /*******************************************************************************************************************
     *
     * Converts a property value from String to its expected value. This is because properties are read by unmarshaller
     * as string.
     *
     ******************************************************************************************************************/
    @Nonnull
    /* visible for testing */ static <T> T convertValue (@Nonnull final Key<T> key, @Nonnull final Object value)
      {
        log.trace("convertValue({}, {})", key, value);
        final T result;

        try
          {
            if (key.getType().isAssignableFrom(value.getClass()))
              {
                result = key.getType().cast(value);
              }
            else if ("tags".equals(key.getName())) // workaround as Zephyr stores it as a comma-separated string
              {
                result = (T)List.of(((String)value).split(","));
              }
//            else if (value instanceof List)
//              {
//                final List<Object> list = (List<Object>)value;
//                Class<?> elementType = String.class; // FIXME: should get the generic of the list
//
//                return (T)list.stream()
//                              .map(i -> CONVERTER_MAP.getOrDefault(elementType, o -> o).apply(value))
//                              .collect(toList());
//              }
            else
              {
                result = (T)CONVERTER_MAP.getOrDefault(key.getType(), o -> o).apply((String)value);
              }

            log.trace(">>>> returning {} ({})", result, result.getClass().getName());
            return result;
          }
        catch (Exception e)
          {
            throw new RuntimeException(String.format("Can't convert '%s' to %s(%s)", value, key, key.getType()), e);
          }
      }
  }
