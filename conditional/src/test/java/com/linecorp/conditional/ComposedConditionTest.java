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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComposedConditionTest {

    static final Condition failed = Condition.failed(unused -> new RuntimeException());

    static Stream<Arguments> AND() {
        return Stream.of(
                // true and failed = exception raised
                Arguments.of(trueCondition().and(failed),
                             RuntimeException.class, null),

                // false and failed = false
                Arguments.of(falseCondition().and(failed),
                             null, false),

                // false and (failed and true) = false
                Arguments.of(falseCondition().and(failed.and(trueCondition())),
                             null, false),

                // (true and true) and failed = exception raised
                Arguments.of(trueCondition().and(trueCondition()).and(failed),
                             RuntimeException.class, null),

                // (true and false) and failed = false
                Arguments.of(trueCondition().and(falseCondition()).and(failed),
                             null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("AND")
    void matches_when_operator_AND(Condition condition,
                                   @Nullable Class<? extends Throwable> expectedException,
                                   @Nullable Boolean expectedMatches) throws Throwable {
        final var ctx = ConditionContext.of();
        final ThrowingCallable throwingCallable = () -> condition.matches(ctx);
        if (expectedException != null) {
            assertThatThrownBy(throwingCallable)
                    .isExactlyInstanceOf(expectedException);
        } else {
            requireNonNull(expectedMatches, "expectedMatches");
            throwingCallable.call();
        }
    }

    static Stream<Arguments> OR() {
        return Stream.of(
                // false or failed = exception raised
                Arguments.of(falseCondition().or(failed),
                             RuntimeException.class, null),

                // true or failed = true
                Arguments.of(trueCondition().or(failed),
                             null, true),

                // true or (failed or false) = true
                Arguments.of(trueCondition().or(failed.or(falseCondition())),
                             null, true),

                // (true or false) or failed = true
                Arguments.of(trueCondition().or(falseCondition()).or(failed),
                             null, true),

                // (false or false) or failed = exception raised
                Arguments.of(falseCondition().or(falseCondition()).or(failed),
                             RuntimeException.class, null)
        );
    }

    @ParameterizedTest
    @MethodSource("OR")
    void matches_when_operator_OR(Condition condition,
                                  @Nullable Class<? extends Throwable> expectedException,
                                  @Nullable Boolean expectedMatches) throws Throwable {
        final var ctx = ConditionContext.of();
        final ThrowingCallable throwingCallable = () -> condition.matches(ctx);
        if (expectedException != null) {
            assertThatThrownBy(throwingCallable)
                    .isExactlyInstanceOf(expectedException);
        } else {
            requireNonNull(expectedMatches, "expectedMatches");
            throwingCallable.call();
        }
    }
}
