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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConditionContextTest {

    @Test
    void of() {
        final var ctx = ConditionContext.of();
        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertTrue(conditionExecutionResults.isEmpty());
    }

    @Test
    void conditionExecutionResult1() {
        // true
        final var condition = Condition.trueCondition();
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(1, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());
    }

    @Test
    void conditionExecutionResult2() {
        // false
        final var condition = Condition.falseCondition();
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(1, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult0.condition());
        assertFalse(conditionExecutionResult0.matches());
    }

    @Test
    void conditionExecutionResult3() {
        // exceptional
        final var condition = Condition.exceptional(unused -> new RuntimeException());
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(1, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionFailure) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.exceptional(unused -> new RuntimeException()),
                              conditionExecutionResult0.condition());
        assertRuntimeException(conditionExecutionResult0.cause());
    }

    @Test
    void conditionExecutionResult4() {
        // true && false
        final var condition = Condition.trueCondition().and(Condition.falseCondition());
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(3, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult1.condition());
        assertFalse(conditionExecutionResult1.matches());

        final var conditionExecutionResult2 = (ConditionExecutionCompletion) conditionExecutionResults.get(2);
        assertConditionEquals(Condition.trueCondition().and(Condition.falseCondition()),
                              conditionExecutionResult2.condition());
        assertFalse(conditionExecutionResult2.matches());
    }

    @Test
    void conditionExecutionResult5() {
        // true || false
        final var condition = Condition.trueCondition().or(Condition.falseCondition());
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(2, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.trueCondition().or(Condition.falseCondition()),
                              conditionExecutionResult1.condition());
        assertTrue(conditionExecutionResult1.matches());
    }

    @Test
    void conditionExecutionResult6() {
        // false && true
        final var condition = Condition.falseCondition().and(Condition.trueCondition());
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(2, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult0.condition());
        assertFalse(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.falseCondition().and(Condition.trueCondition()),
                              conditionExecutionResult1.condition());
        assertFalse(conditionExecutionResult1.matches());
    }

    @Test
    void conditionExecutionResult7() {
        // false || true
        final var condition = Condition.falseCondition().or(Condition.trueCondition());
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(3, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult0.condition());
        assertFalse(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult1.condition());
        assertTrue(conditionExecutionResult1.matches());

        final var conditionExecutionResult2 = (ConditionExecutionCompletion) conditionExecutionResults.get(2);
        assertConditionEquals(Condition.falseCondition().or(Condition.trueCondition()),
                              conditionExecutionResult2.condition());
        assertTrue(conditionExecutionResult2.matches());
    }

    @Test
    void conditionExecutionResult8() {
        // true && exceptional
        final var condition = Condition.trueCondition().and(
                Condition.exceptional(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(3, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionFailure) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.exceptional(unused -> new RuntimeException()),
                              conditionExecutionResult1.condition());
        assertRuntimeException(conditionExecutionResult1.cause());

        final var conditionExecutionResult2 = (ConditionExecutionFailure) conditionExecutionResults.get(2);
        assertConditionEquals(
                Condition.trueCondition().and(Condition.exceptional(unused -> new RuntimeException())),
                conditionExecutionResult2.condition());
        assertRuntimeException(conditionExecutionResult2.cause());
    }

    @Test
    void conditionExecutionResult9() {
        // true || exceptional
        final var condition = Condition.trueCondition().or(
                Condition.exceptional(unused -> new RuntimeException()));
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(2, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(
                Condition.trueCondition().or(Condition.exceptional(unused -> new RuntimeException())),
                conditionExecutionResult1.condition());
        assertTrue(conditionExecutionResult1.matches());
    }

    @Test
    void conditionExecutionResult10() {
        // (true && false) || (false || exceptional)
        final var condition = Condition.trueCondition().and(Condition.falseCondition())
                                       .or(Condition.falseCondition().or(
                                               Condition.exceptional(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        assertThrows(RuntimeException.class, () -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(7, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult1.condition());
        assertFalse(conditionExecutionResult1.matches());

        final var conditionExecutionResult2 = (ConditionExecutionCompletion) conditionExecutionResults.get(2);
        assertConditionEquals(Condition.trueCondition().and(Condition.falseCondition()),
                              conditionExecutionResult2.condition());
        assertFalse(conditionExecutionResult2.matches());

        final var conditionExecutionResult3 = (ConditionExecutionCompletion) conditionExecutionResults.get(3);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult3.condition());
        assertFalse(conditionExecutionResult3.matches());

        final var conditionExecutionResult4 = (ConditionExecutionFailure) conditionExecutionResults.get(4);
        assertConditionEquals(Condition.exceptional(unused -> new RuntimeException()),
                              conditionExecutionResult4.condition());
        assertRuntimeException(conditionExecutionResult4.cause());

        final var conditionExecutionResult5 = (ConditionExecutionFailure) conditionExecutionResults.get(5);
        assertConditionEquals(
                Condition.falseCondition().or(Condition.exceptional(unused -> new RuntimeException())),
                conditionExecutionResult5.condition());
        assertRuntimeException(conditionExecutionResult5.cause());

        final var conditionExecutionResult6 = (ConditionExecutionFailure) conditionExecutionResults.get(6);
        assertConditionEquals(Condition.trueCondition().and(Condition.falseCondition())
                                       .or(Condition.falseCondition().or(
                                               Condition.exceptional(unused -> new RuntimeException()))),
                              conditionExecutionResult6.condition());
        assertRuntimeException(conditionExecutionResult6.cause());
    }

    @Test
    void conditionExecutionResult11() {
        // (true && false) || (false && exceptional)
        final var condition = Condition.trueCondition().and(Condition.falseCondition())
                                       .or(Condition.falseCondition().and(
                                               Condition.exceptional(unused -> new RuntimeException())));
        final var ctx = ConditionContext.of();
        Assertions.assertDoesNotThrow(() -> condition.matches(ctx));

        final var conditionExecutionResults = ctx.conditionExecutionResults();
        assertEquals(6, conditionExecutionResults.size());

        final var conditionExecutionResult0 = (ConditionExecutionCompletion) conditionExecutionResults.get(0);
        assertConditionEquals(Condition.trueCondition(), conditionExecutionResult0.condition());
        assertTrue(conditionExecutionResult0.matches());

        final var conditionExecutionResult1 = (ConditionExecutionCompletion) conditionExecutionResults.get(1);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult1.condition());
        assertFalse(conditionExecutionResult1.matches());

        final var conditionExecutionResult2 = (ConditionExecutionCompletion) conditionExecutionResults.get(2);
        assertConditionEquals(Condition.trueCondition().and(Condition.falseCondition()),
                              conditionExecutionResult2.condition());
        assertFalse(conditionExecutionResult2.matches());

        final var conditionExecutionResult3 = (ConditionExecutionCompletion) conditionExecutionResults.get(3);
        assertConditionEquals(Condition.falseCondition(), conditionExecutionResult3.condition());
        assertFalse(conditionExecutionResult3.matches());

        final var conditionExecutionResult4 = (ConditionExecutionCompletion) conditionExecutionResults.get(4);
        assertConditionEquals(
                Condition.falseCondition().and(Condition.exceptional(unused -> new RuntimeException())),
                conditionExecutionResult4.condition());
        assertFalse(conditionExecutionResult4.matches());

        final var conditionExecutionResult5 = (ConditionExecutionCompletion) conditionExecutionResults.get(5);
        assertConditionEquals(Condition.trueCondition().and(Condition.falseCondition())
                                       .or(Condition.falseCondition().and(
                                               Condition.exceptional(unused -> new RuntimeException()))),
                              conditionExecutionResult5.condition());
        assertFalse(conditionExecutionResult5.matches());
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
