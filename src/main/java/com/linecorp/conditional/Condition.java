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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public abstract class Condition {

    private static final ConditionFunction NOOP = ctx -> {
        throw new UnsupportedOperationException("NOOP");
    };

    private static final long DEFAULT_DELAY_MILLIS = 0L;
    private static final long DEFAULT_TIMEOUT_MILLIS = Long.MAX_VALUE;

    private final ConditionFunction function;
    @Nullable
    private final String alias;
    private final boolean async;
    @Nullable
    private final Executor executor;
    private final long delayMillis;
    private final long timeoutMillis;

    protected Condition() {
        this(NOOP);
    }

    Condition(ConditionFunction function) {
        this(function, null, false, null, DEFAULT_DELAY_MILLIS, DEFAULT_TIMEOUT_MILLIS);
    }

    Condition(ConditionFunction function, @Nullable String alias,
              boolean async, @Nullable Executor executor,
              long delayMillis, long timeoutMillis) {
        requireNonNull(function, "function");
        if (delayMillis < 0) {
            throw new IllegalArgumentException("delayMillis: " + delayMillis + " (expected >= 0)");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis: " + timeoutMillis + " (expected > 0)");
        }
        this.function = function;
        this.alias = alias;
        this.async = async;
        this.executor = executor;
        this.delayMillis = delayMillis;
        this.timeoutMillis = timeoutMillis;
    }

    public static final class Aliases {

        public static final String TRUE = "TrueCondition";
        public static final String FALSE = "FalseCondition";
        public static final String COMPLETED = "CompletedCondition";
        public static final String EXCEPTIONAL = "ExceptionalCondition";

        private Aliases() {}
    }

    public static Condition of(ConditionFunction function) {
        return new Condition(function) {
            @Override
            protected boolean match(ConditionContext ctx) {
                return function.match(ctx);
            }
        };
    }

    public static Condition of(ConditionFunction function, long timeoutMillis) {
        return of(function).timeout(timeoutMillis);
    }

    public static Condition of(ConditionFunction function, long timeout, TimeUnit unit) {
        return of(function).timeout(timeout, unit);
    }

    public static Condition async(ConditionFunction function) {
        return async0(function);
    }

    public static Condition async(ConditionFunction function, Executor executor) {
        requireNonNull(executor, "executor");
        return async0(function).executor(executor);
    }

    public static Condition async(ConditionFunction function, long timeoutMillis) {
        return async0(function).timeout(timeoutMillis);
    }

    public static Condition async(ConditionFunction function, long timeoutMillis, Executor executor) {
        requireNonNull(executor, "executor");
        return async0(function).timeout(timeoutMillis).executor(executor);
    }

    public static Condition async(ConditionFunction function, long timeout, TimeUnit unit) {
        return async0(function).timeout(timeout, unit);
    }

    public static Condition async(ConditionFunction function, long timeout, TimeUnit unit, Executor executor) {
        requireNonNull(executor, "executor");
        return async0(function).timeout(timeout, unit).executor(executor);
    }

    private static Condition async0(ConditionFunction function) {
        return of(function).async();
    }

    public static Condition delayed(ConditionFunction function, long delayMillis) {
        return of(function).delay(delayMillis);
    }

    public static Condition delayed(ConditionFunction function, long delay, TimeUnit unit) {
        return of(function).delay(delay, unit);
    }

    public static Condition from(CompletableFuture<Boolean> future) {
        return from0(future);
    }

    public static Condition from(CompletableFuture<Boolean> future, long timeoutMillis) {
        return from0(future).timeout(timeoutMillis);
    }

    public static Condition from(CompletableFuture<Boolean> future, long timeout, TimeUnit unit) {
        return from0(future).timeout(timeout, unit);
    }

    private static Condition from0(CompletableFuture<Boolean> future) {
        requireNonNull(future, "future");
        return of(ctx -> future.join());
    }

    public static Condition from(Supplier<Boolean> supplier) {
        return from0(supplier);
    }

    public static Condition from(Supplier<Boolean> supplier, long timeoutMillis) {
        return from0(supplier).timeout(timeoutMillis);
    }

    public static Condition from(Supplier<Boolean> supplier, long timeout, TimeUnit unit) {
        return from0(supplier).timeout(timeout, unit);
    }

    private static Condition from0(Supplier<Boolean> supplier) {
        requireNonNull(supplier, "supplier");
        return of(ctx -> supplier.get());
    }

    public static Condition from(Function<ConditionContext, Boolean> function) {
        return from0(function);
    }

    public static Condition from(Function<ConditionContext, Boolean> function, long timeoutMillis) {
        return from0(function).timeout(timeoutMillis);
    }

    public static Condition from(Function<ConditionContext, Boolean> function, long timeout, TimeUnit unit) {
        return from0(function).timeout(timeout, unit);
    }

    private static Condition from0(Function<ConditionContext, Boolean> function) {
        requireNonNull(function, "function");
        return of(function::apply);
    }

    public static ComposedCondition allOf(Condition... conditions) {
        return new ComposedCondition(Operator.AND, conditions);
    }

    public static ComposedCondition allOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.AND, conditions);
    }

    public static ComposedCondition anyOf(Condition... conditions) {
        return new ComposedCondition(Operator.OR, conditions);
    }

    public static ComposedCondition anyOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.OR, conditions);
    }

    public static ComposedCondition noneOf(Condition... conditions) {
        return new ComposedCondition(Operator.AND, negateAll(conditions));
    }

    public static ComposedCondition noneOf(List<Condition> conditions) {
        return new ComposedCondition(Operator.AND, negateAll(conditions));
    }

    private static List<Condition> negateAll(Condition... conditions) {
        return negateAll(List.of(conditions));
    }

    private static List<Condition> negateAll(List<Condition> conditions) {
        return conditions.stream().map(Condition::negate).toList();
    }

    public static Condition not(Condition condition) {
        requireNonNull(condition, "condition");
        return of(ctx -> !condition.matches(ctx)).alias("!" + condition);
    }

    public static ConditionBuilder builder() {
        return new ConditionBuilder();
    }

    public static ConditionComposer composer(Operator operator) {
        return new ConditionComposer(operator);
    }

    public static Condition completed(boolean value) {
        return of(ctx -> value).alias(Aliases.COMPLETED);
    }

    public static Condition trueCondition() {
        return of(ctx -> true).alias(Aliases.TRUE);
    }

    public static Condition falseCondition() {
        return of(ctx -> false).alias(Aliases.FALSE);
    }

    public static Condition exceptional(Supplier<? extends RuntimeException> exception) {
        requireNonNull(exception, "exception");
        return of(ctx -> {
            throw exception.get();
        }).alias(Aliases.EXCEPTIONAL);
    }

    public static Condition exceptional(ConditionContextAwareSupplier<? extends RuntimeException> exception) {
        requireNonNull(exception, "exception");
        return of(ctx -> {
            throw exception.get(ctx);
        }).alias(Aliases.EXCEPTIONAL);
    }

    protected ConditionAttributeMutator attributeMutator() {
        return new ConditionAttributeMutator(this);
    }

    private Condition mutate(ConditionAttributeMutatorConsumer consumer) {
        requireNonNull(consumer, "consumer");
        final var mutator = attributeMutator();
        consumer.accept(mutator);
        return mutator.mutate();
    }

    public final Condition function(ConditionFunction function) {
        return mutate(mutator -> mutator.function(function));
    }

    public final ConditionFunction function() {
        return function;
    }

    public final Condition alias(@Nullable String alias) {
        return mutate(mutator -> mutator.alias(alias));
    }

    @Nullable
    public final String alias() {
        return alias;
    }

    public final Condition async() {
        return async(true);
    }

    public final Condition async(boolean async) {
        return mutate(mutator -> mutator.async(async));
    }

    public final boolean isAsync() {
        return async;
    }

    public final Condition executor(@Nullable Executor executor) {
        return mutate(mutator -> mutator.executor(executor));
    }

    @Nullable
    public final Executor executor() {
        return executor;
    }

    public final Condition delay(long delayMillis) {
        return mutate(mutator -> mutator.delay(delayMillis));
    }

    public final Condition delay(long delay, TimeUnit unit) {
        return mutate(mutator -> mutator.delay(delay, unit));
    }

    public final long delayMillis() {
        return delayMillis;
    }

    public final Condition timeout(long timeoutMillis) {
        return mutate(mutator -> mutator.timeout(timeoutMillis));
    }

    public final Condition timeout(long timeout, TimeUnit unit) {
        return mutate(mutator -> mutator.timeout(timeout, unit));
    }

    public final long timeoutMillis() {
        return timeoutMillis;
    }

    public final ComposedCondition and(Condition condition) {
        return composeWith(Operator.AND, condition);
    }

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

    public final Condition negate() {
        return not(this);
    }

    public final boolean matches(ConditionContext ctx) {
        requireNonNull(ctx, "ctx");
        final var startTimeNanos = System.nanoTime();
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
                final var function = this.function;
                return function == NOOP ? match(ctx) : function.match(ctx);
            };
            matches = timeout == DEFAULT_TIMEOUT_MILLIS ?
                      match.get() :
                      CompletableFuture.supplyAsync(match).orTimeout(timeout, TimeUnit.MILLISECONDS).join();
        } catch (Exception e) {
            ctx.addConditionExecutionResult(thread, condition, e, durationMillis(startTimeNanos));
            return rethrow(e);
        }
        ctx.addConditionExecutionResult(thread, condition, matches, durationMillis(startTimeNanos));
        return matches;
    }

    protected abstract boolean match(ConditionContext ctx);

    protected static CompletableFuture<Boolean> supplyAsync(Supplier<Boolean> supplier,
                                                            @Nullable Executor executor) {
        requireNonNull(supplier, "supplier");
        return executor != null ?
               CompletableFuture.supplyAsync(supplier, executor) :
               CompletableFuture.supplyAsync(supplier);
    }

    private static long durationMillis(long startTimeNanos) {
        return (System.nanoTime() - startTimeNanos) / 1_000_000L;
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

    private static <R> R rethrow(Throwable e) {
        return typeErasure(e);
    }

    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(Throwable e) throws T {
        requireNonNull(e, "e");
        throw (T) e;
    }
}
