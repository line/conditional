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

import static com.linecorp.conditional.Condition.exceptional;
import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConditionContextTest {

    @Test
    void of() {
        final var ctx = ConditionContext.of();
        final var logs = ctx.logs();
        assertTrue(logs.isEmpty());
    }

    @Test
    void conditionExecutionResult1() {
        // true
        final var condition = trueCondition();
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(1, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());
    }

    @Test
    void conditionExecutionResult2() {
        // false
        final var condition = falseCondition();
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(1, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertFalse(log0.matches());
    }

    @Test
    void conditionExecutionResult3() {
        // exceptional
        final var condition = exceptional(unused -> new RuntimeException());
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(1, logs.size());

        final var log0 = (ConditionExecutionFailure) logs.get(0);
        assertConditionEquals(exceptional(unused -> new RuntimeException()), log0.condition());
        assertRuntimeException(log0.cause());
    }

    @Test
    void conditionExecutionResult4() {
        // true && false
        final var condition = trueCondition().and(falseCondition());
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(3, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertFalse(log1.matches());

        final var log2 = (ConditionExecutionCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertFalse(log2.matches());
    }

    @Test
    void conditionExecutionResult5() {
        // true || false
        final var condition = trueCondition().or(falseCondition());
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(2, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(trueCondition().or(falseCondition()), log1.condition());
        assertTrue(log1.matches());
    }

    @Test
    void conditionExecutionResult6() {
        // false && true
        final var condition = falseCondition().and(trueCondition());
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(2, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertFalse(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(falseCondition().and(trueCondition()), log1.condition());
        assertFalse(log1.matches());
    }

    @Test
    void conditionExecutionResult7() {
        // false || true
        final var condition = falseCondition().or(trueCondition());
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(3, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(falseCondition(), log0.condition());
        assertFalse(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(trueCondition(), log1.condition());
        assertTrue(log1.matches());

        final var log2 = (ConditionExecutionCompletion) logs.get(2);
        assertConditionEquals(falseCondition().or(trueCondition()), log2.condition());
        assertTrue(log2.matches());
    }

    @Test
    void conditionExecutionResult8() {
        // true && exceptional
        final var condition = trueCondition().and(
                exceptional(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(3, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionFailure) logs.get(1);
        assertConditionEquals(exceptional(unused -> new RuntimeException()), log1.condition());
        assertRuntimeException(log1.cause());

        final var log2 = (ConditionExecutionFailure) logs.get(2);
        assertConditionEquals(
                trueCondition().and(exceptional(unused -> new RuntimeException())),
                log2.condition());
        assertRuntimeException(log2.cause());
    }

    @Test
    void conditionExecutionResult9() {
        // true || exceptional
        final var condition = trueCondition().or(
                exceptional(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(2, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(
                trueCondition().or(exceptional(unused -> new RuntimeException())),
                log1.condition());
        assertTrue(log1.matches());
    }

    @Test
    void conditionExecutionResult10() {
        // (true && false) || (false || exceptional)
        final var condition = trueCondition().and(falseCondition())
                                             .or(falseCondition().or(
                                                     exceptional(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(7, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertFalse(log1.matches());

        final var log2 = (ConditionExecutionCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertFalse(log2.matches());

        final var log3 = (ConditionExecutionCompletion) logs.get(3);
        assertConditionEquals(falseCondition(), log3.condition());
        assertFalse(log3.matches());

        final var log4 = (ConditionExecutionFailure) logs.get(4);
        assertConditionEquals(exceptional(unused -> new RuntimeException()), log4.condition());
        assertRuntimeException(log4.cause());

        final var log5 = (ConditionExecutionFailure) logs.get(5);
        assertConditionEquals(
                falseCondition().or(exceptional(unused -> new RuntimeException())),
                log5.condition());
        assertRuntimeException(log5.cause());

        final var log6 = (ConditionExecutionFailure) logs.get(6);
        assertConditionEquals(
                trueCondition().and(falseCondition())
                               .or(falseCondition().or(exceptional(unused -> new RuntimeException()))),
                log6.condition());
        assertRuntimeException(log6.cause());
    }

    @Test
    void conditionExecutionResult11() {
        // (true && false) || (false && exceptional)
        final var condition = trueCondition().and(falseCondition())
                                             .or(falseCondition().and(
                                                     exceptional(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        assertDoesNotThrow(() -> condition.matches(ctx));

        final var logs = ctx.logs();
        assertEquals(6, logs.size());

        final var log0 = (ConditionExecutionCompletion) logs.get(0);
        assertConditionEquals(trueCondition(), log0.condition());
        assertTrue(log0.matches());

        final var log1 = (ConditionExecutionCompletion) logs.get(1);
        assertConditionEquals(falseCondition(), log1.condition());
        assertFalse(log1.matches());

        final var log2 = (ConditionExecutionCompletion) logs.get(2);
        assertConditionEquals(trueCondition().and(falseCondition()), log2.condition());
        assertFalse(log2.matches());

        final var log3 = (ConditionExecutionCompletion) logs.get(3);
        assertConditionEquals(falseCondition(), log3.condition());
        assertFalse(log3.matches());

        final var log4 = (ConditionExecutionCompletion) logs.get(4);
        assertConditionEquals(
                falseCondition().and(exceptional(unused -> new RuntimeException())),
                log4.condition());
        assertFalse(log4.matches());

        final var log5 = (ConditionExecutionCompletion) logs.get(5);
        assertConditionEquals(
                trueCondition().and(falseCondition())
                               .or(falseCondition().and(exceptional(unused -> new RuntimeException()))),
                log5.condition());
        assertFalse(log5.matches());
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
        assertTrue(raised);
    }
}
