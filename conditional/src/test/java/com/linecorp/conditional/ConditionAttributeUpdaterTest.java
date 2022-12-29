package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.trueCondition;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class ConditionAttributeUpdaterTest {

    @Test
    void alias() {
        final var attributeUpdater = new ConditionAttributeUpdater(trueCondition());
        final var alias = "alias";
        assertThat(attributeUpdater.alias()).isNotEqualTo(alias);
        attributeUpdater.alias(alias);
        assertThat(attributeUpdater.update().alias()).isEqualTo(alias);
    }

    @Test
    void async() {
        final var attributeUpdater = new ConditionAttributeUpdater(trueCondition());
        final var async = true;
        assertThat(attributeUpdater.isAsync()).isNotEqualTo(async);
        attributeUpdater.async(async);
        assertThat(attributeUpdater.update().isAsync()).isEqualTo(async);
    }

    @Test
    void executor() {
        final var attributeUpdater = new ConditionAttributeUpdater(trueCondition());
        final Executor executor = Executors.newSingleThreadExecutor();
        assertThat(attributeUpdater.executor()).isNotEqualTo(executor);
        attributeUpdater.executor(executor);
        assertThat(attributeUpdater.update().executor()).isEqualTo(executor);
    }

    @Test
    void delayMillis() {
        final var attributeUpdater = new ConditionAttributeUpdater(trueCondition());
        final var delayMillis = 1000L;
        assertThat(attributeUpdater.delayMillis()).isNotEqualTo(delayMillis);
        attributeUpdater.delay(delayMillis);
        assertThat(attributeUpdater.update().delayMillis()).isEqualTo(delayMillis);
    }

    @Test
    void timeoutMillis() {
        final var attributeUpdater = new ConditionAttributeUpdater(trueCondition());
        final var timeoutMillis = 1000L;
        assertThat(attributeUpdater.timeoutMillis()).isNotEqualTo(timeoutMillis);
        attributeUpdater.timeout(timeoutMillis);
        assertThat(attributeUpdater.update().timeoutMillis()).isEqualTo(timeoutMillis);
    }
}
