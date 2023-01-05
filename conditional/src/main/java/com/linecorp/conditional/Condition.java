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
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * <h2>Makes and matches conditional expression.</h2>
 *
 * <p>It takes only 3 steps to make and match a conditional expression.</p>
 * <pre>
 * // Step 1: Make a conditional expression.
 * var a = Condition.of(ctx -> ctx.var("a", Boolean.class));
 * var b = Condition.of(ctx -> ctx.var("b", Boolean.class));
 * var condition = a.and(b);
 *
 * // Step 2: Make a context for matching conditional expression.
 * var ctx = ConditionContext.of("a", true, "b", true);
 *
 * // Step 3: Match a conditional expression.
 * assert condition.matches(ctx) == true;</pre>
 * <p>
 * If you need set timeout, use {@link Condition#timeout(long)}, {@link Condition#timeout(long, TimeUnit)}.
 * </p><p>
 * If you need asynchronous support, use {@link Condition#async()} related methods.
 * Alternatively, {@link Condition#parallel()} might also help.
 * </p><p>
 * Note that {@link Condition#matches(ConditionContext)} waits until matches of all nested {@link Condition}s are completed.
 * If you don't want to wait for matches to complete, use {@link Condition#matchesAsync(ConditionContext)} instead.
 * </p>
 */
public abstract class Condition {

    public static final String DEFAULT_ALIAS = null;
    public static final boolean DEFAULT_ASYNC_ENABLED = false;
    public static final Executor DEFAULT_EXECUTOR = null;
    public static final long DEFAULT_DELAY_MILLIS = 0L;
    public static final long DEFAULT_TIMEOUT_MILLIS = Long.MAX_VALUE;
    public static final boolean DEFAULT_CANCELLABLE_ENABLED = false;

    @Nullable
    private final String alias;
    private final boolean async;
    @Nullable
    private final Executor executor;
    private final long delayMillis;
    private final long timeoutMillis;
    private final boolean cancellable;

    protected Condition() {
        this(DEFAULT_ALIAS, DEFAULT_ASYNC_ENABLED, DEFAULT_EXECUTOR,
             DEFAULT_DELAY_MILLIS, DEFAULT_TIMEOUT_MILLIS, DEFAULT_CANCELLABLE_ENABLED);
    }

    Condition(@Nullable String alias, boolean async, @Nullable Executor executor,
              long delayMillis, long timeoutMillis, boolean cancellable) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis: " + delayMillis + " (expected >= 0)");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis: " + timeoutMillis + " (expected > 0)");
        }
        this.alias = alias;
        this.async = async;
        this.executor = executor;
        this.delayMillis = delayMillis;
        this.timeoutMillis = timeoutMillis;
        this.cancellable = cancellable;
    }

    private static final class Aliases {

        private static final String TRUE = "TrueCondition";
        private static final String FALSE = "FalseCondition";
        private static final String COMPLETED = "CompletedCondition";
        private static final String FAILED = "FailedCondition";
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition of(ConditionFunction function) {
        requireNonNull(function, "function");
        return new Condition() {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function.match(ctx);
            }
        };
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition of(ConditionFunction function, long timeoutMillis) {
        return of(function).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code unit} is null.
     */
    public static Condition of(ConditionFunction function, long timeout, TimeUnit unit) {
        return of(function).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the duration to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code timeout} is null.
     */
    public static Condition of(ConditionFunction function, Duration timeout) {
        return of(function).timeout(timeout);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set alias for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition of(ConditionFunction function, String alias) {
        requireNonNull(alias, "alias");
        return of(function).alias(alias);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set alias for the {@code function}.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition of(ConditionFunction function, String alias, long timeoutMillis) {
        return of(function, alias).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code unit} is null.
     */
    public static Condition of(ConditionFunction function, String alias, long timeout, TimeUnit unit) {
        return of(function, alias).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the duration to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code timeout} is null.
     */
    public static Condition of(ConditionFunction function, String alias, Duration timeout) {
        return of(function, alias).timeout(timeout);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition async(ConditionFunction function) {
        return of(function).async();
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition async(ConditionFunction function, long timeoutMillis) {
        return async(function).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, long timeoutMillis, Executor executor) {
        return async(function, executor).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code unit} is null.
     */
    public static Condition async(ConditionFunction function, long timeout, TimeUnit unit) {
        return async(function).timeout(timeout, unit);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code unit} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, long timeout, TimeUnit unit, Executor executor) {
        return async(function, executor).timeout(timeout, unit);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the duration to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code timeout} is null.
     */
    public static Condition async(ConditionFunction function, Duration timeout) {
        return async(function).timeout(timeout);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param timeout the duration to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code timeout} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, Duration timeout, Executor executor) {
        return async(function, executor).timeout(timeout);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, Executor executor) {
        requireNonNull(executor, "executor");
        return async(function).executor(executor);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition async(ConditionFunction function, String alias) {
        requireNonNull(alias, "alias");
        return async(function).alias(alias);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition async(ConditionFunction function, String alias, long timeoutMillis) {
        return async(function, alias).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, String alias, long timeoutMillis,
                                  Executor executor) {
        requireNonNull(executor, "executor");
        return async(function, alias).timeout(timeoutMillis).executor(executor);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code unit} is null.
     */
    public static Condition async(ConditionFunction function, String alias, long timeout, TimeUnit unit) {
        return async(function, alias).timeout(timeout, unit);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code unit} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, String alias, long timeout, TimeUnit unit,
                                  Executor executor) {
        requireNonNull(executor, "executor");
        return async(function, alias).timeout(timeout, unit).executor(executor);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the duration to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code timeout} is null.
     */
    public static Condition async(ConditionFunction function, String alias, Duration timeout) {
        return async(function, alias).timeout(timeout);
    }

    /**
     * Returns a newly created asynchronous {@link Condition}.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param timeout the duration to set timeout for the {@code function}.
     * @param executor the executor to match the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code timeout} or {@code executor} is null.
     */
    public static Condition async(ConditionFunction function, String alias, Duration timeout,
                                  Executor executor) {
        requireNonNull(executor, "executor");
        return async(function, alias).timeout(timeout).executor(executor);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param delayMillis the value to set delay for the {@code function}.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition delayed(ConditionFunction function, long delayMillis) {
        return of(function).delay(delayMillis);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param delay the value to set delay for the {@code function}.
     * @param unit the unit to set delay for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code unit} is null.
     */
    public static Condition delayed(ConditionFunction function, long delay, TimeUnit unit) {
        return of(function).delay(delay, unit);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param delay the duration to set delay for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code delay} is null.
     */
    public static Condition delayed(ConditionFunction function, Duration delay) {
        return of(function).delay(delay);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param delayMillis the value to set delay for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition delayed(ConditionFunction function, String alias, long delayMillis) {
        return of(function, alias).delay(delayMillis);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param delay the value to set delay for the {@code function}.
     * @param unit the unit to set delay for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code unit} is null.
     */
    public static Condition delayed(ConditionFunction function, String alias, long delay, TimeUnit unit) {
        return of(function, alias).delay(delay, unit);
    }

    /**
     * Returns a newly created {@link Condition} with delay.
     *
     * @param function the function to match the conditional expression.
     * @param alias the value to set the alias for the {@code function}.
     * @param delay the duration to set delay for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code delay} is null.
     */
    public static Condition delayed(ConditionFunction function, String alias, Duration delay) {
        return of(function, alias).delay(delay);
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     *
     * @throws NullPointerException if the {@code future} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future) {
        requireNonNull(future, "future");
        return of(ctx -> future.join());
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     * @param alias the value to set alias for the {@code future}.
     *
     * @throws NullPointerException if {@code future} or {@code alias} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future, String alias) {
        requireNonNull(alias, "alias");
        return from(future).alias(alias);
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     * @param alias the value to set alias for the {@code future}.
     * @param timeoutMillis the value to set timeout for the {@code future}.
     *
     * @throws NullPointerException if {@code future} or {@code alias} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future, String alias, long timeoutMillis) {
        return from(future, alias).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     * @param alias the value to set alias for the {@code future}.
     * @param timeout the value to set timeout for the {@code future}.
     * @param unit the unit to set timeout for the {@code future}.
     *
     * @throws NullPointerException if {@code future} or {@code alias} or {@code unit} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future, String alias, long timeout, TimeUnit unit) {
        return from(future, alias).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code future}.
     *
     * @throws NullPointerException if the {@code future} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future, long timeoutMillis) {
        return from(future).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link CompletableFuture}.
     *
     * @param future the {@link CompletableFuture} to match the conditional expression.
     * @param timeout the value to set timeout for the {@code future}.
     * @param unit the unit to set timeout for the {@code future}.
     *
     * @throws NullPointerException if {@code future} or {@code unit} is null.
     */
    public static Condition from(CompletableFuture<Boolean> future, long timeout, TimeUnit unit) {
        return from(future).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     *
     * @throws NullPointerException if the {@code supplier} is null.
     */
    public static Condition from(Supplier<Boolean> supplier) {
        requireNonNull(supplier, "supplier");
        return of(ctx -> supplier.get());
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     * @param alias the value to set alias for the {@code supplier}.
     *
     * @throws NullPointerException if {@code supplier} or {@code alias} is null.
     */
    public static Condition from(Supplier<Boolean> supplier, String alias) {
        requireNonNull(alias, "alias");
        return from(supplier).alias(alias);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     * @param alias the value to set alias for the {@code supplier}.
     * @param timeoutMillis the value to set timeout for the {@code supplier}.
     *
     * @throws NullPointerException if {@code supplier} or {@code alias} is null.
     */
    public static Condition from(Supplier<Boolean> supplier, String alias, long timeoutMillis) {
        return from(supplier, alias).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     * @param alias the value to set alias for the {@code supplier}.
     * @param timeout the value to set timeout for the {@code supplier}.
     * @param unit the unit to set timeout for the {@code supplier}.
     *
     * @throws NullPointerException if {@code supplier} or {@code alias} or {@code unit} is null.
     */
    public static Condition from(Supplier<Boolean> supplier, String alias, long timeout, TimeUnit unit) {
        return from(supplier, alias).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code supplier}.
     *
     * @throws NullPointerException if the {@code supplier} is null.
     */
    public static Condition from(Supplier<Boolean> supplier, long timeoutMillis) {
        return from(supplier).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Supplier}.
     *
     * @param supplier the {@link Supplier} to match the conditional expression.
     * @param timeout the value to set timeout for the {@code supplier}.
     * @param unit the unit to set timeout for the {@code supplier}.
     *
     * @throws NullPointerException if {@code supplier} or {@code unit} is null.
     */
    public static Condition from(Supplier<Boolean> supplier, long timeout, TimeUnit unit) {
        return from(supplier).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function) {
        requireNonNull(function, "function");
        return of(function::apply);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     * @param alias the value to set alias for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function, String alias) {
        return from(function).alias(alias);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     * @param alias the value to set alias for the {@code function}.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function, String alias,
                                 long timeoutMillis) {
        return from(function, alias).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     * @param alias the value to set alias for the {@code function}.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code alias} or {@code unit} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function, String alias,
                                 long timeout, TimeUnit unit) {
        return from(function, alias).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     * @param timeoutMillis the value to set timeout for the {@code function}.
     *
     * @throws NullPointerException if the {@code function} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function, long timeoutMillis) {
        return from(function).timeout(timeoutMillis);
    }

    /**
     * Returns a newly created {@link Condition} from {@link Function}.
     *
     * @param function the {@link Function} to match the conditional expression.
     * @param timeout the value to set timeout for the {@code function}.
     * @param unit the unit to set timeout for the {@code function}.
     *
     * @throws NullPointerException if {@code function} or {@code unit} is null.
     */
    public static Condition from(Function<ConditionContext, Boolean> function, long timeout, TimeUnit unit) {
        return from(function).timeout(timeout, unit);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the AND operator.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition allOf(Condition... conditions) {
        return new ComposedCondition(Operator.AND, conditions);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the AND operator.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws NullPointerException if the {@code conditions} is null.
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition allOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.AND, conditions);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the OR operator.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition anyOf(Condition... conditions) {
        return new ComposedCondition(Operator.OR, conditions);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the OR operator.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws NullPointerException if the {@code conditions} is null.
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition anyOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.OR, conditions);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the AND operator.
     * All {@code conditions} passed as parameters are negated.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition noneOf(Condition... conditions) {
        return new ComposedCondition(Operator.AND, negateAll(conditions));
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the AND operator.
     * All {@code conditions} passed as parameters are negated.
     *
     * @param conditions the {@link Condition}s to compose.
     *
     * @throws NullPointerException if the {@code conditions} is null.
     * @throws IllegalArgumentException if the {@code conditions} is empty.
     */
    public static ComposedCondition noneOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.AND, negateAll(conditions));
    }

    private static List<Condition> negateAll(Condition... conditions) {
        return negateAll(List.of(conditions));
    }

    private static List<Condition> negateAll(List<Condition> conditions) {
        return conditions.stream().map(Condition::negate).toList();
    }

    /**
     * Returns a newly created negative {@link Condition}.
     *
     * @param condition the {@code condition} to negate.
     *
     * @throws NullPointerException if the {@code condition} is null.
     */
    public static Condition not(Condition condition) {
        requireNonNull(condition, "condition");
        return of(ctx -> !condition.matches(ctx)).alias("!" + condition);
    }

    /**
     * Returns the {@link ConditionBuilder}.
     */
    public static ConditionBuilder builder() {
        return new ConditionBuilder();
    }

    /**
     * Returns the {@link ConditionComposer}.
     *
     * @param operator the operator of {@link ComposedCondition}.
     *
     * @throws NullPointerException if the {@code operator} is null.
     */
    public static ConditionComposer composer(Operator operator) {
        return new ConditionComposer(operator);
    }

    /**
     * Returns a newly created {@link Condition} by specific {@code value}.
     */
    public static Condition completed(boolean value) {
        return of(ctx -> value).alias(Aliases.COMPLETED);
    }

    /**
     * Returns a newly created {@link Condition} with {@code true}.
     */
    public static Condition trueCondition() {
        return of(ctx -> true).alias(Aliases.TRUE);
    }

    /**
     * Returns a newly created {@link Condition} with {@code false}.
     */
    public static Condition falseCondition() {
        return of(ctx -> false).alias(Aliases.FALSE);
    }

    /**
     * Returns a newly created {@link Condition} by {@code e}.
     *
     * @throws NullPointerException if the {@code e} is null.
     */
    public static Condition failed(Throwable e) {
        requireNonNull(e, "e");
        return of(ctx -> rethrow(e)).alias(Aliases.FAILED);
    }

    /**
     * Returns a newly created {@link Condition} by {@code exceptionSupplier}.
     *
     * @throws NullPointerException if the {@code exceptionSupplier} is null.
     */
    public static Condition failed(Supplier<? extends Throwable> exceptionSupplier) {
        requireNonNull(exceptionSupplier, "exceptionSupplier");
        return of(ctx -> rethrow(exceptionSupplier.get())).alias(Aliases.FAILED);
    }

    /**
     * Returns a newly created {@link Condition} by {@code exceptionSupplier}.
     *
     * @throws NullPointerException if the {@code exceptionSupplier} is null.
     */
    public static Condition failed(
            ConditionContextAwareSupplier<? extends Throwable> exceptionSupplier) {
        requireNonNull(exceptionSupplier, "exceptionSupplier");
        return of(ctx -> rethrow(exceptionSupplier.get(ctx))).alias(Aliases.FAILED);
    }

    protected ConditionAttributeUpdater attributeUpdater() {
        return new ConditionAttributeUpdater(this);
    }

    private Condition update(ConditionAttributeUpdaterConsumer attributeUpdaterConsumer) {
        requireNonNull(attributeUpdaterConsumer, "attributeUpdaterConsumer");
        final var attributeUpdater = attributeUpdater();
        attributeUpdaterConsumer.accept(attributeUpdater);
        return attributeUpdater.update();
    }

    /**
     * Returns the {@link Condition} with {@code alias} updated.
     */
    public Condition alias(@Nullable String alias) {
        return update(attributeUpdater -> attributeUpdater.alias(alias));
    }

    /**
     * Returns the {@code alias}.
     */
    @Nullable
    public final String alias() {
        return alias;
    }

    /**
     * Returns the {@link Condition} with {@code async} enabled.
     */
    public Condition async() {
        return async(true);
    }

    /**
     * Returns the {@link Condition} with {@code async} updated.
     */
    public Condition async(boolean async) {
        return update(attributeUpdater -> attributeUpdater.async(async));
    }

    /**
     * Returns whether {@code async} is enabled.
     */
    public final boolean isAsync() {
        return async;
    }

    /**
     * Returns the {@link Condition} with {@code executor} updated.
     */
    public Condition executor(@Nullable Executor executor) {
        return update(attributeUpdater -> attributeUpdater.executor(executor));
    }

    /**
     * Returns the {@link Executor}.
     */
    @Nullable
    public final Executor executor() {
        return executor;
    }

    /**
     * Returns the {@link Condition} with {@code delayMillis} attribute updated.
     */
    public Condition delay(long delayMillis) {
        return update(attributeUpdater -> attributeUpdater.delay(delayMillis));
    }

    /**
     * Returns the {@link Condition} with {@code delayMillis} attribute updated.
     *
     * @throws NullPointerException if the {@code unit} is null.
     */
    public Condition delay(long delay, TimeUnit unit) {
        return update(attributeUpdater -> attributeUpdater.delay(delay, unit));
    }

    /**
     * Returns the {@link Condition} with {@code delayMillis} attribute updated.
     *
     * @throws NullPointerException if the {@code delay} is null.
     */
    public Condition delay(Duration delay) {
        return update(attributeUpdater -> attributeUpdater.delay(delay));
    }

    /**
     * Returns the {@code delayMillis}.
     */
    public final long delayMillis() {
        return delayMillis;
    }

    /**
     * Returns the {@link Condition} with {@code timeoutMillis} attribute updated.
     */
    public Condition timeout(long timeoutMillis) {
        return update(attributeUpdater -> attributeUpdater.timeout(timeoutMillis));
    }

    /**
     * Returns the {@link Condition} with {@code timeoutMillis} attribute updated.
     *
     * @throws NullPointerException if the {@code unit} is null.
     */
    public Condition timeout(long timeout, TimeUnit unit) {
        return update(attributeUpdater -> attributeUpdater.timeout(timeout, unit));
    }

    /**
     * Returns the {@link Condition} with {@code timeoutMillis} attribute updated.
     *
     * @throws NullPointerException if the {@code timeout} is null.
     */
    public Condition timeout(Duration timeout) {
        return update(attributeUpdater -> attributeUpdater.timeout(timeout));
    }

    /**
     * Returns the {@code timeoutMillis}.
     */
    public final long timeoutMillis() {
        return timeoutMillis;
    }

    /**
     * Returns the {@link Condition} with {@code cancellable} set to specific value
     * for all nested {@link Condition}s and {@link Condition} itself.
     *
     * @see Condition#matches(ConditionContext)
     */
    public final Condition cancellable(boolean cancellable) {
        if (this instanceof ComposedCondition this0) {
            return this0.update(attributeUpdater -> {
                attributeUpdater.cancellable(cancellable);
                attributeUpdater.conditions(this0.conditions().stream().map(
                        condition -> condition.cancellable(cancellable)).toList());
            });
        } else {
            return update(attributeUpdater -> attributeUpdater.cancellable(cancellable));
        }
    }

    /**
     * Returns the {@code cancellable}.
     */
    public final boolean cancellable() {
        return cancellable;
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the AND operator.
     *
     * @param condition the {@link Condition} to compose.
     *
     * @throws NullPointerException if the {@code condition} is null.
     */
    public final ComposedCondition and(Condition condition) {
        return composeWith(Operator.AND, condition);
    }

    /**
     * Returns a newly created {@link ComposedCondition}.
     * This {@link ComposedCondition} is composed with the OR operator.
     *
     * @param condition the {@link Condition} to compose.
     *
     * @throws NullPointerException if the {@code condition} is null.
     */
    public final ComposedCondition or(Condition condition) {
        return composeWith(Operator.OR, condition);
    }

    private ComposedCondition composeWith(Operator operator, Condition condition) {
        requireNonNull(operator, "operator");
        requireNonNull(condition, "condition");
        if (this instanceof ComposedCondition this0) {
            final var concatWith = this0.operator() == operator && !this0.isAsync();
            if (concatWith) {
                return this0.add(condition);
            }
        }
        return new ComposedCondition(operator, this, condition);
    }

    /**
     * Returns a newly created negative {@link Condition}.
     */
    public final Condition negate() {
        return not(this);
    }

    /**
     * Returns the {@link Condition} with {@code async} disabled
     * for all nested {@link Condition}s and {@link Condition} itself.
     */
    public final Condition sequential() {
        return colored(false, null);
    }

    /**
     * Returns the {@link Condition} with {@code async} enabled
     * for all nested {@link Condition}s and {@link Condition} itself.
     */
    public final Condition parallel() {
        return colored(true, null);
    }

    /**
     * Returns the {@link Condition} with {@code async} enabled
     * for all nested {@link Condition}s and {@link Condition} itself.
     *
     * @param executor the executor to match all nested {@link Condition}s.
     *
     * @throws NullPointerException if the {@code executor} is null.
     */
    public final Condition parallel(Executor executor) {
        requireNonNull(executor, "executor");
        return colored(true, executor);
    }

    private Condition colored(boolean async, @Nullable Executor executor) {
        if (this instanceof ComposedCondition this0) {
            return this0.update(attributeUpdater -> {
                attributeUpdater.async(async).executor(async ? executor : null);
                attributeUpdater.conditions(this0.conditions().stream().map(
                        condition -> condition.colored(async, executor)).toList());
            });
        } else {
            return update(attributeUpdater -> attributeUpdater.async(async).executor(async ? executor : null));
        }
    }

    /**
     * Returns the matched result of the {@link Condition}.
     * If the timeout is exceeded, a {@link TimeoutException} is raised.
     *
     * @param ctx the context for matching {@link Condition}.
     *
     * @throws NullPointerException if the {@code ctx} is null.
     * @throws IllegalStateException if the {@code delay} is greater than or equal to {@code timeout}.
     * @throws CancellationException if the {@code cancellable} of {@link Condition} is {@code true},
     * and the {@link CompletableFuture} is cancelled because an exception is raised in a certain {@link Condition}.
     * @see Condition#cancellable(boolean)
     */
    public final boolean matches(ConditionContext ctx) {
        requireNonNull(ctx, "ctx");
        final var startTimeMillis = System.currentTimeMillis();
        final var thread = Thread.currentThread();
        final var condition = this;
        final boolean matches;
        try {
            final var delay = delayMillis;
            final var timeout = timeoutMillis;
            assert delay >= 0 && timeout > 0;
            if (delay >= timeout) {
                throw new IllegalStateException("delay >= timeout (expected delay < timeout)");
            }
            final Supplier<Boolean> match = () -> {
                if (delay > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException e) {
                        return rethrow(e);
                    }
                }
                return match(ctx);
            };
            matches = timeout == DEFAULT_TIMEOUT_MILLIS ?
                      match.get() :
                      CompletableFuture.supplyAsync(match).orTimeout(timeout, TimeUnit.MILLISECONDS).join();
        } catch (Exception e) {
            Throwable cause = e;
            if (e instanceof CompletionException) {
                cause = e.getCause();
            }
            if (cause instanceof CancellationException) {
                ctx.cancelled(thread, condition, cause, startTimeMillis, System.currentTimeMillis());
            } else if (cause instanceof TimeoutException) {
                ctx.timedOut(thread, condition, cause, startTimeMillis, System.currentTimeMillis());
            } else {
                ctx.failed(thread, condition, cause, startTimeMillis, System.currentTimeMillis());
            }
            return rethrow(cause);
        }
        ctx.completed(thread, condition, matches, startTimeMillis, System.currentTimeMillis());
        return matches;
    }

    /**
     * Returns the {@link CompletableFuture}.
     * The returned {@link CompletableFuture} will be notified when the {@link Condition} is matched.
     *
     * @param ctx the context for matching {@link Condition}.
     */
    public final CompletableFuture<Boolean> matchesAsync(ConditionContext ctx) {
        return matchesAsync0(ctx, null);
    }

    /**
     * Returns the {@link CompletableFuture}.
     * The returned {@link CompletableFuture} will be notified when the {@link Condition} is matched.
     *
     * @param ctx the context for matching {@link Condition}.
     * @param executor the executor to match the {@link ConditionFunction}.
     */
    public final CompletableFuture<Boolean> matchesAsync(ConditionContext ctx, Executor executor) {
        requireNonNull(executor, "executor");
        return matchesAsync0(ctx, executor);
    }

    private CompletableFuture<Boolean> matchesAsync0(ConditionContext ctx, @Nullable Executor executor) {
        requireNonNull(ctx, "ctx");
        return supplyAsync(() -> matches(ctx), executor);
    }

    protected abstract boolean match(ConditionContext ctx);

    protected static CompletableFuture<Boolean> supplyAsync(Supplier<Boolean> supplier,
                                                            @Nullable Executor executor) {
        requireNonNull(supplier, "supplier");
        return executor != null ?
               CompletableFuture.supplyAsync(supplier, executor) :
               CompletableFuture.supplyAsync(supplier);
    }

    @Override
    public String toString() {
        final var alias = this.alias;
        return alias != null && !alias.isBlank() ? alias : classNameOf(this, "Undefined");
    }

    private static String classNameOf(Object obj, String defaultValue) {
        requireNonNull(obj, "obj");
        requireNonNull(defaultValue, "defaultValue");
        final var className = obj.getClass().getSimpleName();
        return !className.isBlank() ? className : defaultValue;
    }

    protected static <R> R rethrow(Throwable e) {
        return typeErasure(e);
    }

    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(Throwable e) throws T {
        requireNonNull(e, "e");
        throw (T) e;
    }
}
