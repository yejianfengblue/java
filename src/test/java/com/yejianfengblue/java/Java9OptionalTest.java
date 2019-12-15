package com.yejianfengblue.java;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author yejianfengblue
 */
class Java9OptionalTest {
    /**
     * In Java 8, when the Optional is empty, and get default value from
     * some other action which also returns an Optional, but Optional class
     * had only the T orElse(T) and T orElseGet(Supplier<T>) and both return
     * unwrapped values.
     * Java 9 introduces the Optional<T> or(Supplier<Optional<T>>) which
     * returns another Optional lazily if the Optional is empty.
     */
    @Nested
    class OptionalOr {

        @Test
        void givenJava8Optional_whenPresent_thenTakeAnUnwrappedValueFromOrElse() {

            // given
            Optional<String> emptyOptional = Optional.empty();
            String defaultValue = "default";

            // when
            String result = emptyOptional.orElse(defaultValue);

            // then
            assertEquals(defaultValue, result);
        }

        @Test
        void givenJava8Optional_whenPresent_thenTakeAnUnwrappedValueFromOrElseGet() {

            // given
            Optional<String> emptyOptional = Optional.empty();
            String defaultValue = "default";

            // when
            String result = emptyOptional.orElseGet(() -> defaultValue);

            // then
            assertEquals(defaultValue, result);
        }

        @Test
        void givenJava9Optional_whenPresent_thenTakeAValueFromIt() {

            // given
            String expected = "properValue";
            Optional<String> expectedOptional = Optional.of(expected);
            Optional<String> defaultValueOptional = Optional.of("default");

            // when
            Optional<String> result = expectedOptional.or(() -> defaultValueOptional);

            // then
            assertEquals(expected, result.get());
        }

        @Test
        void givenJava9Optional_whenEmpty_thenTakeAValueFromOr() {

            // given
            String defaultValue = "default";
            Optional<String> emptyOptional = Optional.empty();
            Optional<String> defaultValueOptional = Optional.of(defaultValue);

            // when
            Optional<String> result = emptyOptional.or(() -> defaultValueOptional);

            // then
            assertEquals(defaultValue, result.get());
        }
    }

    @Nested
    class OptionalIfPresentOrElse {

        @Test
        void givenOptional_whenPresent_thenExecuteConsumer() {

            // given
            Optional<String> notEmptyOptional = Optional.of("definedValue");
            AtomicInteger successCounter = new AtomicInteger(0);
            AtomicInteger onEmptyOptionalCounter = new AtomicInteger(0);

            // when
            // if present, call the first consumer with defined value
            // if empty, run the second runnable (runnable is also a Functional Interface)
            notEmptyOptional.ifPresentOrElse(
                    v -> successCounter.incrementAndGet(),
                    onEmptyOptionalCounter::incrementAndGet
            );

            // then
            assertEquals(1, successCounter.get());
            assertEquals(0, onEmptyOptionalCounter.get());
        }

        @Test
        void givenOptional_whenNotPresent_thenExecuteSecondCallback() {

            // given
            Optional<String> emptyOptional = Optional.empty();
            AtomicInteger successCounter = new AtomicInteger(0);
            AtomicInteger onEmptyOptionalCounter = new AtomicInteger(0);

            // when
            emptyOptional.ifPresentOrElse(
                    v -> successCounter.incrementAndGet(),
                    onEmptyOptionalCounter::incrementAndGet
            );

            // then
            assertEquals(0, successCounter.get());
            assertEquals(1, onEmptyOptionalCounter.get());
        }
    }
}
