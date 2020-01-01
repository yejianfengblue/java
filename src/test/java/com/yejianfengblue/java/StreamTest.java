package com.yejianfengblue.java;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author yejianfengblue
 */
class StreamTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Test
    void givenStream_whenPeekWithConsumer_thenThisConsumerAppliesToEachElementInTheStream() {

        // given
        Stream<String> stream = Stream.of("a", "b", "c");

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilderAfterMap = new StringBuilder();

        // when
        // peek() is mainly for debug purpose
        stream
                .peek(stringBuilder::append)
                .map(String::toUpperCase)
                .peek(stringBuilderAfterMap::append)
                .collect(Collectors.toList());

        // then
        assertEquals("abc", stringBuilder.toString());
        assertEquals("ABC", stringBuilderAfterMap.toString());
    }

    @Test
    void givenTwoStream_whenConcat_thenGetANewStream() {

        // given
        Stream<String> oddIntStream = Stream.of("1", "3", "5");
        Stream<String> evenIntStream = Stream.of("2", "4", "6");

        // when
        Stream<String> combinedIntStream = Stream.concat(oddIntStream, evenIntStream);

        // then
        assertEquals("135246", combinedIntStream.collect(Collectors.joining()));
    }

    @Test
    void givenIntStream_whenReduceWithAddOperation_thenTheResultIsTheSumOfAllElementInStream() {

        // given
        IntStream intStream = IntStream.rangeClosed(1, 10);

        // when
        int result = intStream.reduce((i1, i2) -> {
            return i1 + i2;
        }).getAsInt();

        // then
        assertEquals(55, result);
    }

    /**
     * {@code Stream.reduce()} has three forms:
     * <ul>
     *     <li>{@link Stream#reduce(BinaryOperator accumulator)}</li>
     *     <li>{@link Stream#reduce(Object identity, BinaryOperator accumulator)}</li>
     *     <li>{@link Stream#reduce(Object identity, BiFunction accumulator, BinaryOperator combiner)}</li>
     * </ul>
     * The three roles:
     * <ul>
     *     <li>identity - an element that is the initial value of the reduction op and the default result
     *     when empty stream, and for all x, accumulator.apply(identity, x) is equal to x</li>
     *     <li>accumulator - a function that take two params: a partial result of the reduction op, and
     *     the next element of the stream</li>
     *     <li>combiner - a function used to combine the partial result of the reduction op when the stream is parallel,
     *     or when the type of the two accumulator args are diff</li>
     * </ul>
     */
    @Nested
    class ReduceTest {

        @Test
        void givenStream_whenReduceWithAccumulator_thenResultEqualsThisAccumulatorAppliesToAllElementsAndReturnOptional() {

            // given
            Stream<String> stringStream = Stream.of("a", "b", "c");

            // when
            /* Stream<T> public abstract Optional<T> reduce(BinaryOperator<T> accumulator)
             * The type of accumulator is same as the element in the stream
             */
            Optional<String> resultString = stringStream.reduce((s1, s2) -> s1 + s2);

            // then
            assertTrue(resultString.isPresent());
            assertEquals("abc", resultString.get());
        }

        @Test
        void givenEmptyStream_whenReduceWithIdentity_thenResultEqualsIdentity() {

            // given
            IntStream intStream = IntStream.empty();

            // when
            int result = intStream.reduce(0, (i1, i2) -> {
                return i1 + i2;
            });

            // then
            assertEquals(0, result);
        }

        @Test
        void givenStream_whenReduceWithIdentityAndAccumulator_thenResultEqualsThisAccumulatorAppliesToAllElements() {

            // given
            Stream<String> stringStream = Stream.of("a", "b", "c");

            // when
            /* Stream<T> public abstract T reduce(T identity, BinaryOperator<T> accumulator)
             * The accumulator takes two params of same type
             */
            String resultString = stringStream.reduce("", (s1, s2) -> s1 + s2);

            // then
            assertEquals("abc", resultString);
        }

        /**
         * The reduce() method can be a combination of map() and aggregate
         */
        @Test
        void givenStream_whenReduceWithIdentityAndAccumulatorAndCombiner_whenCanAggregateToDiffType() {

            // now if we want to count the total length of string in the stream, we can use map() and reduce()
            Optional<Integer> totalStringLengthOptional = Stream.of("a", "b", "c").map(String::length).reduce(Integer::sum);
            assertEquals(3, totalStringLengthOptional.get());

            /* Stream<T> <U> U reduce(U identity,
             *                        BiFunction<U, ? super T, U> accumulator,
             *                        BinaryOperator<U> combiner)
             * Stream<String> Integer reduce(Integer identity,
             *                               BiFunction<Integer, ? super String, Integer> accumulator,
             *                               BinaryOperator<Integer> combiner)
             * The 2th param of the accumulator is the next element of the stream, so has the same type T of stream
             */
            int totalLength = Stream.of("a", "b", "c").reduce(0,
                    (partialResult, nextElem) -> partialResult + nextElem.length(),
                    Integer::sum);

            // then
            assertEquals(3, totalLength);
        }
    }
}
