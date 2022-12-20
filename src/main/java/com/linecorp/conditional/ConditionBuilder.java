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

public final class ConditionBuilder {

    private volatile ConditionFunction function;
    @Nullable
    private volatile String alias;
    private volatile boolean async;
    @Nullable
    private volatile Executor executor;
    private volatile long delayMillis;
    private volatile long timeoutMillis;

    ConditionBuilder() {}

    public ConditionBuilder function(ConditionFunction function) {
        this.function = requireNonNull(function, "function");
        return this;
    }

    public ConditionBuilder alias(@Nullable String alias) {
        this.alias = alias;
        return this;
    }

    public ConditionBuilder async() {
        return async(true);
    }

    public ConditionBuilder async(boolean async) {
        this.async = async;
        return this;
    }

    public ConditionBuilder executor(@Nullable Executor executor) {
        this.executor = executor;
        return this;
    }

    public ConditionBuilder delay(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    public ConditionBuilder delay(long delay, TimeUnit unit) {
        requireNonNull(unit, "unit");
        delayMillis = unit.toMillis(delay);
        return this;
    }

    public ConditionBuilder timeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public ConditionBuilder timeout(long timeout, TimeUnit unit) {
        requireNonNull(unit, "unit");
        timeoutMillis = unit.toMillis(timeout);
        return this;
    }

    public Condition build() {
        return new Condition(function, alias, async, executor, delayMillis, timeoutMillis) {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function().match(ctx);
            }
        };
    }
}
