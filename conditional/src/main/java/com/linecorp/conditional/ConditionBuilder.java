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

import static com.linecorp.conditional.Condition.DEFAULT_ALIAS;
import static com.linecorp.conditional.Condition.DEFAULT_ASYNC_ENABLED;
import static com.linecorp.conditional.Condition.DEFAULT_CANCELLABLE_ENABLED;
import static com.linecorp.conditional.Condition.DEFAULT_DELAY_MILLIS;
import static com.linecorp.conditional.Condition.DEFAULT_EXECUTOR;
import static com.linecorp.conditional.Condition.DEFAULT_TIMEOUT_MILLIS;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

public final class ConditionBuilder {

    @Nullable
    private volatile String alias = DEFAULT_ALIAS;
    private volatile boolean async = DEFAULT_ASYNC_ENABLED;
    @Nullable
    private volatile Executor executor = DEFAULT_EXECUTOR;
    private volatile long delayMillis = DEFAULT_DELAY_MILLIS;
    private volatile long timeoutMillis = DEFAULT_TIMEOUT_MILLIS;
    private volatile boolean cancellable = DEFAULT_CANCELLABLE_ENABLED;

    ConditionBuilder() {}

    /**
     * Returns the {@link ConditionBuilder} with {@code alias} set.
     */
    public ConditionBuilder alias(@Nullable String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code async} set to {@code true}.
     */
    public ConditionBuilder async() {
        return async(true);
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code async} set to specific value.
     */
    public ConditionBuilder async(boolean async) {
        this.async = async;
        return this;
    }

    /**
     * (Advanced users only) Returns the {@link ConditionBuilder} with {@code executor} set.
     * Note that if asynchronous {@link Condition}s are composed in a nested way,
     * deadlock may occur if the {@code executor} is incorrectly specified.
     * If you don't know what this means, do not use this method.
     */
    public ConditionBuilder executor(@Nullable Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code delayMillis} set.
     */
    public ConditionBuilder delay(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code delay} and {@code unit} set.
     *
     * @throws NullPointerException if the {@code unit} is null.
     */
    public ConditionBuilder delay(long delay, TimeUnit unit) {
        requireNonNull(unit, "unit");
        delayMillis = unit.toMillis(delay);
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code timeoutMillis} set.
     */
    public ConditionBuilder timeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code timeout} and {@code unit} set.
     *
     * @throws NullPointerException if the {@code unit} is null.
     */
    public ConditionBuilder timeout(long timeout, TimeUnit unit) {
        requireNonNull(unit, "unit");
        timeoutMillis = unit.toMillis(timeout);
        return this;
    }

    /**
     * Returns the {@link ConditionBuilder} with {@code cancellable} set.
     */
    public ConditionBuilder cancellable(boolean cancellable) {
        this.cancellable = cancellable;
        return this;
    }

    /**
     * Returns a newly created {@link Condition} by {@link ConditionBuilder}.
     */
    public Condition build(ConditionFunction function) {
        requireNonNull(function, "function");
        return new Condition(alias, async, executor, delayMillis, timeoutMillis, cancellable) {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function.match(ctx);
            }
        };
    }
}
