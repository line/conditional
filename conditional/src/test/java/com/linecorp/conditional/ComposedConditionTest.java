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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
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

    @Test
    void avoid_deadlock() {
        final var executor = Executors.newSingleThreadExecutor();
        final var a = Condition.of(ctx -> true).delay(1000).alias("a");
        final var b = Condition.of(ctx -> true).delay(1000).alias("b");
        final var c = Condition.of(ctx -> true).delay(1000).alias("c");
        final var d = Condition.of(ctx -> true).delay(1000).alias("d");
        final var e = Condition.of(ctx -> true).delay(1000).alias("e");
        final var f = Condition.of(ctx -> true).delay(1000).alias("f");
        final var g = Condition.of(ctx -> true).delay(1000).alias("g");
        final var h = Condition.of(ctx -> true).delay(1000).alias("h");
        final var i = Condition.of(ctx -> true).delay(1000).alias("i");
        final var j = Condition.of(ctx -> true).delay(1000).alias("j");
        final var k = Condition.of(ctx -> true).delay(1000).alias("k");
        final var l = Condition.of(ctx -> true).delay(1000).alias("l");
        final var m = Condition.of(ctx -> true).delay(1000).alias("m");
        final var n = Condition.of(ctx -> true).delay(1000).alias("n");
        final var o = Condition.of(ctx -> true).delay(1000).alias("o");
        final var p = Condition.of(ctx -> true).delay(1000).alias("p");
        final var q = Condition.of(ctx -> true).delay(1000).alias("q");
        final var r = Condition.of(ctx -> true).delay(1000).alias("r");
        final var s = Condition.of(ctx -> true).delay(1000).alias("s");
        final var t = Condition.of(ctx -> true).delay(1000).alias("t");
        final var u = Condition.of(ctx -> true).delay(1000).alias("u");
        final var v = Condition.of(ctx -> true).delay(1000).alias("v");
        final var w = Condition.of(ctx -> true).delay(1000).alias("w");
        final var x = Condition.of(ctx -> true).delay(1000).alias("x");
        final var y = Condition.of(ctx -> true).delay(1000).alias("y");
        final var z = Condition.of(ctx -> true).delay(1000).alias("z");
        final var condition =
                a.and(b.and(c.and(d.and(e.and(f.and(g.and(
                         h.and(i.and(j.and(k.and(l.and(m.and(n.and(
                                 o.and(p.and(q.and(r.and(s.and(t.and(u.and(
                                         v.and(w.and(x.and(y.and(z)))))))))))))))))))))))))
                 .parallel(executor)
                 .timeout(30, TimeUnit.SECONDS);
        final var ctx = ConditionContext.of();
        assertThat(condition.matches(ctx)).isTrue();
    }
}
