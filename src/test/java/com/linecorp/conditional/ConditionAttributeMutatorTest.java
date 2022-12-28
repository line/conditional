package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.trueCondition;
import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(mutator.function()).isNotEqualTo(function);
        mutator.function(function);
        assertThat(mutator.mutate().function()).isEqualTo(function);
    }

    @Test
    void alias() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var alias = "alias";
        assertThat(mutator.alias()).isNotEqualTo(alias);
        mutator.alias(alias);
        assertThat(mutator.mutate().alias()).isEqualTo(alias);
    }

    @Test
    void async() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var async = true;
        assertThat(mutator.isAsync()).isNotEqualTo(async);
        mutator.async(async);
        assertThat(mutator.mutate().isAsync()).isEqualTo(async);
    }

    @Test
    void executor() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final Executor executor = Executors.newSingleThreadExecutor();
        assertThat(mutator.executor()).isNotEqualTo(executor);
        mutator.executor(executor);
        assertThat(mutator.mutate().executor()).isEqualTo(executor);
    }

    @Test
    void delayMillis() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var delayMillis = 1000L;
        assertThat(mutator.delayMillis()).isNotEqualTo(delayMillis);
        mutator.delay(delayMillis);
        assertThat(mutator.mutate().delayMillis()).isEqualTo(delayMillis);
    }

    @Test
    void timeoutMillis() {
        final var mutator = new ConditionAttributeMutator(trueCondition());
        final var timeoutMillis = 1000L;
        assertThat(mutator.timeoutMillis()).isNotEqualTo(timeoutMillis);
        mutator.timeout(timeoutMillis);
        assertThat(mutator.mutate().timeoutMillis()).isEqualTo(timeoutMillis);
    }
}
