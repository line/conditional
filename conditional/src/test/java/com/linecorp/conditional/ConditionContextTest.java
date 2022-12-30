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

import static com.linecorp.conditional.Condition.failed;
import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ConditionContextTest {

    @Test
    void of() {
        final var ctx = ConditionContext.of();
        final var logs = ctx.logs();
        assertThat(logs.isEmpty()).isTrue();
    }

    @Test
    void copy() {
        final var a = Condition.of(ctx -> ctx.var("a", Boolean.class));
        final var b = Condition.of(ctx -> ctx.var("b", Boolean.class));
        final var condition = a.and(b);
        final var ctx = ConditionContext.of("a", true, "b", true);
        assertThat(condition.matches(ctx)).isTrue();
        assertThat(ctx.var("a", Boolean.class)).isTrue();
        assertThat(ctx.var("b", Boolean.class)).isTrue();
        assertThat(ctx.logs()).isNotEmpty();

        final var copy = ctx.copy();
        assertThat(copy.var("a", Boolean.class)).isTrue();
        assertThat(copy.var("b", Boolean.class)).isTrue();
        assertThat(copy.logs()).isEmpty();
        assertThat(condition.matches(copy)).isTrue();
        assertThat(copy.logs()).isNotEmpty();
    }

    @Test
    void conditionMatchResult1() {
        // true
        final var condition = trueCondition();
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(1);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();
    }

    @Test
    void conditionMatchResult2() {
        // false
        final var condition = falseCondition();
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(1);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertThat(log0.matches()).isFalse();
    }

    @Test
    void conditionMatchResult3() {
        // failed
        final var condition = failed(unused -> new RuntimeException());
        final var ctx = ConditionContext.of();
        assertThatThrownBy(() -> condition.matches(ctx))
                .isExactlyInstanceOf(RuntimeException.class);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(1);

        final var log0 = (ConditionMatchFailure) logs.get(0);
        assertConditionEquals(failed(unused -> new RuntimeException()), log0.condition());
        assertRuntimeException(log0.cause());
    }

    @Test
    void conditionMatchResult4() {
        // true and false
        final var condition = trueCondition().and(falseCondition());
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(3);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertThat(log1.matches()).isFalse();

        final var log2 = (ConditionMatchCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertThat(log2.matches()).isFalse();
    }

    @Test
    void conditionMatchResult5() {
        // true or false
        final var condition = trueCondition().or(falseCondition());
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(2);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(trueCondition().or(falseCondition()), log1.condition());
        assertThat(log1.matches()).isTrue();
    }

    @Test
    void conditionMatchResult6() {
        // false and true
        final var condition = falseCondition().and(trueCondition());
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(2);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertThat(log0.matches()).isFalse();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(falseCondition().and(trueCondition()), log1.condition());
        assertThat(log1.matches()).isFalse();
    }

    @Test
    void conditionMatchResult7() {
        // false or true
        final var condition = falseCondition().or(trueCondition());
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(3);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertThat(log0.matches()).isFalse();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(trueCondition(), log1.condition());
        assertThat(log1.matches()).isTrue();

        final var log2 = (ConditionMatchCompletion) logs.get(2);
        assertConditionEquals(falseCondition().or(trueCondition()), log2.condition());
        assertThat(log2.matches()).isTrue();
    }

    @Test
    void conditionMatchResult8() {
        // true and failed
        final var condition = trueCondition().and(failed(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        assertThatThrownBy(() -> condition.matches(ctx))
                .isExactlyInstanceOf(RuntimeException.class);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(3);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchFailure) logs.get(1);
        assertConditionEquals(failed(unused -> new RuntimeException()), log1.condition());
        assertRuntimeException(log1.cause());

        final var log2 = (ConditionMatchFailure) logs.get(2);
        assertConditionEquals(
                trueCondition().and(failed(unused -> new RuntimeException())),
                log2.condition());
        assertRuntimeException(log2.cause());
    }

    @Test
    void conditionMatchResult9() {
        // true or failed
        final var condition = trueCondition().or(failed(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(2);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(
                trueCondition().or(failed(unused -> new RuntimeException())),
                log1.condition());
        assertThat(log1.matches()).isTrue();
    }

    @Test
    void conditionMatchResult10() {
        // (true and false) or (false or failed)
        final var condition = trueCondition().and(falseCondition())
                                             .or(falseCondition().or(failed(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        assertThatThrownBy(() -> condition.matches(ctx))
                .isExactlyInstanceOf(RuntimeException.class);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(7);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertThat(log1.matches()).isFalse();

        final var log2 = (ConditionMatchCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertThat(log2.matches()).isFalse();

        final var log3 = (ConditionMatchCompletion) logs.get(3);
        assertConditionEquals(falseCondition(), log3.condition());
        assertThat(log3.matches()).isFalse();

        final var log4 = (ConditionMatchFailure) logs.get(4);
        assertConditionEquals(failed(unused -> new RuntimeException()), log4.condition());
        assertRuntimeException(log4.cause());

        final var log5 = (ConditionMatchFailure) logs.get(5);
        assertConditionEquals(
                falseCondition().or(failed(unused -> new RuntimeException())),
                log5.condition());
        assertRuntimeException(log5.cause());

        final var log6 = (ConditionMatchFailure) logs.get(6);
        assertConditionEquals(
                trueCondition().and(falseCondition())
                               .or(falseCondition().or(failed(unused -> new RuntimeException()))),
                log6.condition());
        assertRuntimeException(log6.cause());
    }

    @Test
    void conditionMatchResult11() {
        // (true and false) or (false and failed)
        final var condition = trueCondition().and(falseCondition())
                                             .or(falseCondition().and(
                                                     failed(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        condition.matches(ctx);

        final var logs = ctx.logs();
        assertThat(logs.size()).isEqualTo(6);

        final var log0 = (ConditionMatchCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertThat(log0.matches()).isTrue();

        final var log1 = (ConditionMatchCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertThat(log1.matches()).isFalse();

        final var log2 = (ConditionMatchCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertThat(log2.matches()).isFalse();

        final var log3 = (ConditionMatchCompletion) logs.get(3);
        assertConditionEquals(falseCondition(), log3.condition());
        assertThat(log3.matches()).isFalse();

        final var log4 = (ConditionMatchCompletion) logs.get(4);
        assertConditionEquals(
                falseCondition().and(failed(unused -> new RuntimeException())),
                log4.condition());
        assertThat(log4.matches()).isFalse();

        final var log5 = (ConditionMatchCompletion) logs.get(5);
        assertConditionEquals(
                trueCondition().and(falseCondition())
                               .or(falseCondition().and(failed(unused -> new RuntimeException()))),
                log5.condition());
        assertThat(log5.matches()).isFalse();
    }

    static void assertConditionEquals(Condition expected, Condition actual) {
        requireNonNull(expected, "expected");
        requireNonNull(actual, "actual");
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    static void assertRuntimeException(Throwable e) {
        boolean raised = false;
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof RuntimeException) {
                raised = true;
                break;
            }
        }
        assertThat(raised).isTrue();
    }
}
