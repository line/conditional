package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.trueCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class ConditionAttributeMutatorTest {

    @Test
    void function() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var function = new ConditionFunction() {
            @Override
            public boolean match(ConditionContext ctx) {
                throw new UnsupportedOperationException();
            }
        };
        assertNotEquals(function, mutator.function());
        mutator.function(function);
        assertEquals(function, mutator.mutate().function());
    }

    @Test
    void alias() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var alias = "alias";
        assertNotEquals(alias, mutator.alias());
        mutator.alias(alias);
        assertEquals(alias, mutator.mutate().alias());
    }

    @Test
    void async() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var async = true;
        assertNotEquals(async, mutator.isAsync());
        mutator.async(async);
        assertEquals(async, mutator.mutate().isAsync());
    }

    @Test
    void executor() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final Executor executor = Executors.newSingleThreadExecutor();
        assertNotEquals(executor, mutator.executor());
        mutator.executor(executor);
        assertEquals(executor, mutator.mutate().executor());
    }

    @Test
    void delayMillis() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var delayMillis = 1000L;
        assertNotEquals(delayMillis, mutator.delayMillis());
        mutator.delay(delayMillis);
        assertEquals(delayMillis, mutator.mutate().delayMillis());
    }

    @Test
    void timeoutMillis() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var timeoutMillis = 1000L;
        assertNotEquals(timeoutMillis, mutator.timeoutMillis());
        mutator.timeout(timeoutMillis);
        assertEquals(timeoutMillis, mutator.mutate().timeoutMillis());
    }
}