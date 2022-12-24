/*
 * Copyright 2022 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComposedConditionTest {

    static final Condition exceptional = Condition.exceptional(unused -> new RuntimeException());

    static Stream<Arguments> AND() {
        return Stream.of(
                // true && exceptional = exception raised
                Arguments.of(trueCondition().and(exceptional),
                             RuntimeException.class, null),

                // false && exceptional = false
                Arguments.of(falseCondition().and(exceptional),
                             null, false),

                // false && (exceptional && true) = false
                Arguments.of(falseCondition().and(exceptional.and(trueCondition())),
                             null, false),

                // (true && true) && exceptional = exception raised
                Arguments.of(trueCondition().and(trueCondition()).and(exceptional),
                             RuntimeException.class, null),

                // (true && false) && exceptional = false
                Arguments.of(trueCondition().and(falseCondition()).and(exceptional),
                             null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("AND")
    void matches_when_operator_AND(Condition condition,
                                   @Nullable Class<? extends Throwable> expectedException,
                                   @Nullable Boolean expectedMatches) {
        final var ctx = ConditionContext.of();
        final Executable executable = () -> condition.matches(ctx);
        if (expectedException != null) {
            assertThrows(expectedException, executable);
        } else {
            requireNonNull(expectedMatches, "expectedMatches");
            assertDoesNotThrow(executable);
        }
    }

    static Stream<Arguments> OR() {
        return Stream.of(
                // false || exceptional = exception raised
                Arguments.of(falseCondition().or(exceptional),
                             RuntimeException.class, null),

                // true || exceptional = true
                Arguments.of(trueCondition().or(exceptional),
                             null, true),

                // true || (exceptional || false) = true
                Arguments.of(trueCondition().or(exceptional.or(falseCondition())),
                             null, true),

                // (true || false) || exceptional = true
                Arguments.of(trueCondition().or(falseCondition()).or(exceptional),
                             null, true),

                // (false || false) || exceptional = exception raised
                Arguments.of(falseCondition().or(falseCondition()).or(exceptional),
                             RuntimeException.class, null)
        );
    }

    @ParameterizedTest
    @MethodSource("OR")
    void matches_when_operator_OR(Condition condition,
                                  @Nullable Class<? extends Throwable> expectedException,
                                  @Nullable Boolean expectedMatches) {
        final var ctx = ConditionContext.of();
        final Executable executable = () -> condition.matches(ctx);
        if (expectedException != null) {
            assertThrows(expectedException, executable);
        } else {
            requireNonNull(expectedMatches, "expectedMatches");
            assertDoesNotThrow(executable);
        }
    }

    static Stream<Arguments> SEQUENTIAL() {
        final var a = Condition.delayed(ctx -> true, 2000, TimeUnit.MILLISECONDS).alias("a");
        final var b = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).alias("b");
        return Stream.of(
                Arguments.of(a.and(b).sequential(), 5000, 5500),
                Arguments.of(a.or(b).sequential(), 2000, 2500));
    }

    @ParameterizedTest
    @MethodSource("SEQUENTIAL")
    void sequential(Condition condition, long atLeastMillis, long atMostMillis) {
        final var ctx = ConditionContext.of();
        await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   assertTrue(condition.matches(ctx));
                   return true;
               });
    }

    static Stream<Arguments> PARALLEL() {
        final var a = Condition.delayed(ctx -> true, 2000, TimeUnit.MILLISECONDS).alias("a");
        final var b = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).alias("b");
        final var executor = Executors.newSingleThreadExecutor();
        return Stream.of(
                Arguments.of(a.and(b).parallel(), 3000, 3500),
                Arguments.of(a.or(b).parallel(), 2000, 2500),
                Arguments.of(a.and(b).parallel(executor), 5000, 5500),
                Arguments.of(a.or(b).parallel(executor), 2000, 3500));
    }

    @ParameterizedTest
    @MethodSource("PARALLEL")
    void parallel(Condition condition, long atLeastMillis, long atMostMillis) {
        final var ctx = ConditionContext.of();
        await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   assertTrue(condition.matches(ctx));
                   return true;
               });
    }
}
