/*
 * #%L
 * *********************************************************************************************************************
 *
 * NorthernWind - lightweight CMS
 * http://northernwind.tidalwave.it - git clone https://bitbucket.org/tidalwave/northernwind-src.git
 * %%
 * Copyright (C) 2011 - 2021 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.northernwind.frontend.ui.component.calendar.htmltemplate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @ToString @EqualsAndHashCode // TODO: push up to TFT
public class Pair<A, B>
  {
    private static final IntUnaryOperator base0 = i -> i;
    private static final IntUnaryOperator base1 = i -> i + 1;

    public final A a;
    public final B b;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <A, B> Pair<A, B> of (final @Nonnull A a, final @Nonnull B b)
      {
        return new Pair<>(a, b);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <A, B> Collector<? super Pair<A, B>, ?, Map<A, B>> pairsToMap()
      {
        return Collectors.toMap(p -> p.a, p -> p.b);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements in the array, made of {@link Pair}s {@code (index, value)}.
     *
     * @param       <T>             the type of the elements
     * @param       array           the array
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> Stream<Pair<Integer, T>> indexedPairStream (final @Nonnull T[] array)
      {
        return Pair.indexedPairStream(array, base0, i -> i);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements in the array, made of {@link Pair}s {@code (index, value)}. The index
     * can be transformed with specific functions.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       array           the array
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T, S> Stream<Pair<S, T>> indexedPairStream (final @Nonnull T[] array,
                                                               final @Nonnull IntFunction<S> indexFunction)
      {
        return Pair.indexedPairStream(array, base0, indexFunction);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements in the array, made of {@link Pair}s {@code (index, value)}.
     * The index starts with 1, not 0.
     *
     * @param       <T>             the type of the elements
     * @param       array           the array
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> Stream<Pair<Integer, T>> indexedPairStream1 (final @Nonnull T[] array)
      {
        return Pair.indexedPairStream(array, base1, i -> i);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements in the array, made of {@link Pair}s {@code (index, value)}. The index
     * can be transformed with specific functions.
     * The index starts with 1, not 0.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       array           the array
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T, S> Stream<Pair<S, T>> indexedPairStream1 (final @Nonnull T[] array,
                                                                final @Nonnull IntFunction<S> indexFunction)
      {
        return Pair.indexedPairStream(array, base1, indexFunction);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements returned by a supplier, made of {@link Pair}s
     * {@code (index, value)}.
     *
     * @param       <T>             the type of the elements
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @param       valueSupplier   the supplier of values
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> Stream<Pair<Integer, T>> indexedPairStream (final @Nonnegative int from,
                                                                  final @Nonnegative int to,
                                                                  final @Nonnull IntFunction<T> valueSupplier)
      {
        return indexedPairStream(from, to, valueSupplier, base0, i -> i);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements returned by a supplier, made of {@link Pair}s
     * {@code (index, value)}. The index can be transformed with specific functions.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @param       valueSupplier   the supplier of values
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T, S> Stream<Pair<S, T>> indexedPairStream (final @Nonnegative int from,
                                                               final @Nonnegative int to,
                                                               final @Nonnull IntFunction<T> valueSupplier,
                                                               final @Nonnull IntFunction<S> indexFunction)
      {
        return indexedPairStream(from, to, valueSupplier, base0, indexFunction);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements returned by a supplier, made of {@link Pair}s
     * {@code (index, value)}.
     * The index starts with 1, not 0.
     *
     * @param       <T>             the type of the elements
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @param       valueSupplier   the supplier of values
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T> Stream<Pair<Integer, T>> indexedPairStream1 (final @Nonnegative int from,
                                                                   final @Nonnegative int to,
                                                                   final @Nonnull IntFunction<T> valueSupplier)
      {
        return indexedPairStream(from, to, valueSupplier, base1, i -> i);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements returned by a supplier, made of {@link Pair}s
     * {@code (index, value)}. The index can be transformed with specific functions.
     * The index starts with 1, not 0.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @param       valueSupplier   the supplier of values
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    public static <T, S> Stream<Pair<S, T>> indexedPairStream1 (final @Nonnegative int from,
                                                                final @Nonnegative int to,
                                                                final @Nonnull IntFunction<T> valueSupplier,
                                                                final @Nonnull IntFunction<S> indexFunction)
      {
        return indexedPairStream(from, to, valueSupplier, base1, indexFunction);
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements in the array, made of {@link Pair}s {@code (index, value)}. The index
     * can be transformed with specific functions.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       array           the array
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    private static <T, S> Stream<Pair<S, T>> indexedPairStream (final @Nonnull T[] array,
                                                                final @Nonnull IntUnaryOperator rebaser,
                                                                final @Nonnull IntFunction<S> indexFunction)
      {
        return IntStream.range(0, array.length).mapToObj(i -> of(indexFunction.apply(rebaser.applyAsInt(i)), array[i]));
      }

    /*******************************************************************************************************************
     *
     * Returns a {@link Stream} out of the elements returned by a supplier, made of {@link Pair}s
     * {@code (index, value)}. The index can be rebased and transformed with specific functions.
     *
     * @param       <T>             the type of the elements
     * @param       <S>             the type of the transformed index
     * @param       from            the first index (included)
     * @param       to              the last index (excluded)
     * @param       valueSupplier   the supplier of values
     * @param       rebaser         the rebaser of the index (pass i -> i+1 to have a 1-based collection)
     * @param       indexFunction   the transformer of the index
     * @return                      the stream
     *
     ******************************************************************************************************************/
    @Nonnull
    private static <T, S> Stream<Pair<S, T>> indexedPairStream (final @Nonnegative int from,
                                                                final @Nonnegative int to,
                                                                final @Nonnull IntFunction<T> valueSupplier,
                                                                final @Nonnull IntUnaryOperator rebaser,
                                                                final @Nonnull IntFunction<S> indexFunction)
      {
        return IntStream.range(from, to).mapToObj(i -> Pair.of(indexFunction.apply(rebaser.applyAsInt(i)), valueSupplier.apply(i)));
      }
  }
