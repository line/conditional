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

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

class ConditionAttributeMutator {

    protected volatile ConditionFunction function;
    @Nullable
    protected volatile String alias;
    protected volatile boolean async;
    @Nullable
    protected volatile Executor executor;
    protected volatile long delayMillis;
    protected volatile long timeoutMillis;

    ConditionAttributeMutator(Condition condition) {
        requireNonNull(condition, "condition");
        function = condition.function();
        alias = condition.alias();
        async = condition.isAsync();
        executor = condition.executor();
        delayMillis = condition.delayMillis();
        timeoutMillis = condition.timeoutMillis();
    }

    final ConditionAttributeMutator function(ConditionFunction function) {
        this.function = requireNonNull(function, "function");
        return this;
    }

    final ConditionAttributeMutator alias(@Nullable String alias) {
        this.alias = alias;
        return this;
    }

    final ConditionAttributeMutator async(boolean async) {
        this.async = async;
        return this;
    }

    final ConditionAttributeMutator executor(@Nullable Executor executor) {
        this.executor = executor;
        return this;
    }

    final ConditionAttributeMutator delay(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    final ConditionAttributeMutator delay(long delay, TimeUnit unit) {
        requireNonNull(unit, "unit");
        delayMillis = unit.toMillis(delay);
        return this;
    }

    final ConditionAttributeMutator timeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    final ConditionAttributeMutator timeout(long timeout, TimeUnit unit) {
        requireNonNull(unit, "unit");
        timeoutMillis = unit.toMillis(timeout);
        return this;
    }

    Condition mutate() {
        return new Condition(function, alias, async, executor, delayMillis, timeoutMillis) {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function().match(ctx);
            }
        };
    }
}
