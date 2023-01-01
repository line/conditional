/*
 * Copyright 2023 LINE Corporation
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class ConditionBuilderTest {

    @Test
    void defaultValues() {
        final var builder = new ConditionBuilder();
        final var condition = builder.build(ctx -> true);
        assertThat(condition.alias()).isEqualTo(Condition.DEFAULT_ALIAS);
        assertThat(condition.isAsync()).isEqualTo(Condition.DEFAULT_ASYNC_ENABLED);
        assertThat(condition.executor()).isEqualTo(Condition.DEFAULT_EXECUTOR);
        assertThat(condition.delayMillis()).isEqualTo(Condition.DEFAULT_DELAY_MILLIS);
        assertThat(condition.timeoutMillis()).isEqualTo(Condition.DEFAULT_TIMEOUT_MILLIS);
        assertThat(condition.cancellable()).isEqualTo(Condition.DEFAULT_CANCELLABLE_ENABLED);
    }

    @Test
    void async() {
        final var builder = new ConditionBuilder();
        builder.async(true);
        final var condition = builder.build(ctx -> true);
        assertThat(condition.isAsync()).isTrue();
    }

    @Test
    void executor() {
        final var executor = Executors.newWorkStealingPool();
        final var builder = new ConditionBuilder();
        builder.executor(executor);
        final var condition = builder.build(ctx -> true);
        assertThat(condition.executor()).isEqualTo(executor);
    }

    @Test
    void delayMillis() {
        final var builder = new ConditionBuilder();
        builder.delay(1000);
        final var condition = builder.build(ctx -> true);
        assertThat(condition.delayMillis()).isEqualTo(1000);
    }

    @Test
    void timeoutMillis() {
        final var builder = new ConditionBuilder();
        builder.timeout(1000);
        final var condition = builder.build(ctx -> true);
        assertThat(condition.timeoutMillis()).isEqualTo(1000);
    }

    @Test
    void cancellable() {
        final var builder = new ConditionBuilder();
        builder.cancellable(true);
        final var condition = builder.build(ctx -> true);
        assertThat(condition.cancellable()).isTrue();
    }
}
