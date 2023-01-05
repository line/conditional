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

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

class ConditionAttributeUpdater {

    private final ConditionFunction function;
    @Nullable
    private volatile String alias;
    private volatile boolean async;
    @Nullable
    private volatile Executor executor;
    private volatile long delayMillis;
    private volatile long timeoutMillis;
    private volatile boolean cancellable;

    ConditionAttributeUpdater(Condition condition) {
        requireNonNull(condition, "condition");
        function = condition::match;
        alias = condition.alias();
        async = condition.isAsync();
        executor = condition.executor();
        delayMillis = condition.delayMillis();
        timeoutMillis = condition.timeoutMillis();
        cancellable = condition.cancellable();
    }

    final ConditionAttributeUpdater alias(@Nullable String alias) {
        this.alias = alias;
        return this;
    }

    @Nullable
    final String alias() {
        return alias;
    }

    final ConditionAttributeUpdater async(boolean async) {
        this.async = async;
        return this;
    }

    final boolean isAsync() {
        return async;
    }

    final ConditionAttributeUpdater executor(@Nullable Executor executor) {
        this.executor = executor;
        return this;
    }

    @Nullable
    final Executor executor() {
        return executor;
    }

    final ConditionAttributeUpdater delay(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    final ConditionAttributeUpdater delay(long delay, TimeUnit unit) {
        requireNonNull(unit, "unit");
        delayMillis = unit.toMillis(delay);
        return this;
    }

    final ConditionAttributeUpdater delay(Duration delay) {
        requireNonNull(delay, "delay");
        delayMillis = delay.toMillis();
        return this;
    }

    final long delayMillis() {
        return delayMillis;
    }

    final ConditionAttributeUpdater timeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    final ConditionAttributeUpdater timeout(long timeout, TimeUnit unit) {
        requireNonNull(unit, "unit");
        timeoutMillis = unit.toMillis(timeout);
        return this;
    }

    final ConditionAttributeUpdater timeout(Duration timeout) {
        requireNonNull(timeout, "timeout");
        timeoutMillis = timeout.toMillis();
        return this;
    }

    final long timeoutMillis() {
        return timeoutMillis;
    }

    final ConditionAttributeUpdater cancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    final boolean cancellable() {
        return cancellable;
    }

    Condition update() {
        return new Condition(alias, async, executor, delayMillis, timeoutMillis, cancellable) {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function.match(ctx);
            }
        };
    }
}
