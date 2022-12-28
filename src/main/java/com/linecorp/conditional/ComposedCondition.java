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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class ComposedCondition extends Condition {

    private static final String PREFIX = "(";
    private static final String SUFFIX = ")";
    private static final String DELIMITER_AND = " and ";
    private static final String DELIMITER_OR = " or ";

    private final Operator operator;
    private final List<Condition> conditions = new ArrayList<>();
    private final boolean cancellable;

    ComposedCondition(Operator operator, Condition... conditions) {
        this(operator, List.of(conditions));
    }

    ComposedCondition(Operator operator, List<Condition> conditions) {
        this(operator, conditions, false);
    }

    ComposedCondition(Operator operator, List<Condition> conditions, boolean cancellable) {
        checkConstructorArguments(operator, conditions);
        this.operator = operator;
        this.conditions.addAll(conditions);
        this.cancellable = cancellable;
    }

    ComposedCondition(ConditionFunction function, @Nullable String alias,
                      boolean async, @Nullable Executor executor,
                      long delayMillis, long timeoutMillis,
                      Operator operator, List<Condition> conditions, boolean cancellable) {
        super(function, alias, async, executor, delayMillis, timeoutMillis);
        checkConstructorArguments(operator, conditions);
        this.operator = operator;
        this.conditions.addAll(conditions);
        this.cancellable = cancellable;
    }

    private static void checkConstructorArguments(Operator operator, List<Condition> conditions) {
        requireNonNull(operator, "operator");
        requireNonNull(conditions, "conditions");
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions is empty (expected not empty)");
        }
    }

    ComposedCondition add(Condition condition) {
        requireNonNull(condition, "condition");
        conditions.add(condition);
        return this;
    }

    @Override
    protected ComposedConditionAttributeMutator attributeMutator() {
        return new ComposedConditionAttributeMutator(this);
    }

    private ComposedCondition mutate(ComposedConditionAttributeMutatorConsumer consumer) {
        requireNonNull(consumer, "consumer");
        final var mutator = attributeMutator();
        consumer.accept(mutator);
        return mutator.mutate();
    }

    Operator operator() {
        return operator;
    }

    List<Condition> conditions() {
        return conditions;
    }

    boolean cancellable() {
        return cancellable;
    }

    /**
     * Returns the {@link ComposedCondition} with function mutated.
     *
     * @throws NullPointerException if the {@code function} is null.
     * @see Condition#function(ConditionFunction)
     */
    @Override
    public ComposedCondition function(ConditionFunction function) {
        return (ComposedCondition) super.function(function);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code alias} mutated.
     *
     * @see Condition#alias(String)
     */
    @Override
    public ComposedCondition alias(@Nullable String alias) {
        return (ComposedCondition) super.alias(alias);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code async} enabled.
     *
     * @see Condition#async()
     */
    @Override
    public ComposedCondition async() {
        return (ComposedCondition) super.async();
    }

    /**
     * Returns the {@link ComposedCondition} with {@code async} mutated.
     *
     * @see Condition#async(boolean)
     */
    @Override
    public ComposedCondition async(boolean async) {
        return (ComposedCondition) super.async(async);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code executor} mutated.
     *
     * @see Condition#executor(Executor)
     */
    @Override
    public ComposedCondition executor(@Nullable Executor executor) {
        return (ComposedCondition) super.executor(executor);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code delay} attribute mutated.
     *
     * @see Condition#delay(long)
     */
    @Override
    public ComposedCondition delay(long delayMillis) {
        return (ComposedCondition) super.delay(delayMillis);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code delay} attribute mutated.
     *
     * @throws NullPointerException if the {@code unit} is null.
     * @see Condition#delay(long, TimeUnit)
     */
    @Override
    public ComposedCondition delay(long delay, TimeUnit unit) {
        return (ComposedCondition) super.delay(delay, unit);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code timeout} attribute mutated.
     *
     * @see Condition#timeout(long)
     */
    @Override
    public ComposedCondition timeout(long timeoutMillis) {
        return (ComposedCondition) super.timeout(timeoutMillis);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code timeout} attribute mutated.
     *
     * @throws NullPointerException if the {@code unit} is null.
     * @see Condition#timeout(long, TimeUnit)
     */
    @Override
    public ComposedCondition timeout(long timeout, TimeUnit unit) {
        return (ComposedCondition) super.timeout(timeout, unit);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code async} disabled for all nested {@link Condition}s.
     */
    public ComposedCondition sequential() {
        return mutateAll(false, null);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code async} enabled for all nested {@link Condition}s.
     */
    public ComposedCondition parallel() {
        return mutateAll(true, null);
    }

    /**
     * Returns the {@link ComposedCondition} with {@code async} enabled for all nested {@link Condition}s.
     *
     * @param executor the executor to match all nested {@link Condition}s.
     *
     * @throws NullPointerException if the {@code executor} is null.
     */
    public ComposedCondition parallel(Executor executor) {
        requireNonNull(executor, "executor");
        return mutateAll(true, executor);
    }

    private ComposedCondition mutateAll(boolean async, @Nullable Executor executor) {
        return mutate(mutator -> mutator.conditions(async(conditions, async, executor)));
    }

    private static List<Condition> async(List<Condition> conditions, boolean async,
                                         @Nullable Executor executor) {
        requireNonNull(conditions, "conditions");
        return conditions.stream().map(condition -> {
            final var condition0 =
                    condition instanceof ComposedCondition composedCondition ?
                    composedCondition.mutate(mutator -> mutator.conditions(
                            async(composedCondition.conditions, async, executor))) : condition;
            return condition0.async(async).executor(async ? executor : null);
        }).toList();
    }

    /**
     * Returns the {@link ComposedCondition} with {@code cancellable} set to specific value.
     *
     * @see Condition#matches(ConditionContext)
     */
    public ComposedCondition cancellable(boolean cancellable) {
        return mutate(mutator -> mutator.cancellable(cancellable));
    }

    @Override
    protected boolean match(ConditionContext ctx) {
        assert !conditions.isEmpty();
        final var cfs = new ArrayList<CompletableFuture<Boolean>>();
        for (var condition : conditions) {
            final CompletableFuture<Boolean> cf;
            try {
                cf = matches0(condition, ctx).exceptionally(e -> cancelWith(cfs, () -> rethrow(e)));
            } catch (Exception e) {
                return cancelWith(cfs, () -> rethrow(e));
            }
            if (completed(cf)) {
                return cancelWith(cfs, cf::join);
            }
            cfs.add(cf);
        }
        try {
            if (completedAsync(cfs).join()) {
                return cancelWith(cfs, () -> switch (operator) {
                    case AND -> false;
                    case OR -> true;
                });
            }
        } catch (Exception e) {
            return cancelWith(cfs, () -> rethrow(e));
        }
        final var it = cfs.iterator();
        var value = it.next().join();
        while (it.hasNext()) {
            final var next = it.next().join();
            value = switch (operator) {
                case AND -> value && next;
                case OR -> value || next;
            };
        }
        return value;
    }

    private static CompletableFuture<Boolean> matches0(Condition condition, ConditionContext ctx) {
        requireNonNull(condition, "condition");
        requireNonNull(ctx, "ctx");
        final Supplier<Boolean> matches = () -> condition.matches(ctx);
        return condition.isAsync() ?
               supplyAsync(matches, condition.executor()) :
               CompletableFuture.completedFuture(matches.get());
    }

    private boolean completed(CompletableFuture<Boolean> cf) {
        requireNonNull(cf, "cf");
        return cf.isDone() ? shortCircuit(operator, cf.join()) : false;
    }

    private CompletableFuture<Boolean> completedAsync(List<CompletableFuture<Boolean>> cfs) {
        requireNonNull(cfs, "cfs");
        final var future = new CompletableFuture<Boolean>();
        final var completed = new AtomicBoolean(false);
        for (var cf : cfs) {
            if (completed.get()) {
                break;
            }
            cf.whenComplete((value, e) -> {
                if (e != null && completed.compareAndSet(false, true)) {
                    completeExceptionally(future, e);
                    cancel(cfs);
                    return;
                }
                if (shortCircuit(operator, value) && completed.compareAndSet(false, true)) {
                    complete(future, true);
                }
            });
        }
        if (!completed.get()) {
            CompletableFuture.allOf(cfs.toArray(CompletableFuture[]::new))
                             .thenRun(() -> {
                                 if (completed.compareAndSet(false, true)) {
                                     complete(future, false);
                                 }
                             });
        }
        return future;
    }

    private static boolean shortCircuit(Operator operator, boolean value) {
        requireNonNull(operator, "operator");
        return switch (operator) {
            case AND -> !value;
            case OR -> value;
        };
    }

    private static void complete(CompletableFuture<Boolean> cf, boolean value) {
        requireNonNull(cf, "cf");
        if (!cf.isDone()) {
            cf.complete(value);
        }
    }

    private static void completeExceptionally(CompletableFuture<Boolean> cf, Throwable e) {
        requireNonNull(cf, "cf");
        requireNonNull(e, "e");
        if (!cf.isDone()) {
            cf.completeExceptionally(e);
        }
    }

    private void cancel(List<CompletableFuture<Boolean>> cfs) {
        requireNonNull(cfs, "cfs");
        if (!cancellable) {
            return;
        }
        for (var cf : cfs) {
            if (!cf.isDone()) {
                cf.cancel(false);
            }
        }
    }

    private <R> R cancelWith(List<CompletableFuture<Boolean>> cfs, Supplier<R> supplier) {
        requireNonNull(supplier, "supplier");
        cancel(cfs);
        return supplier.get();
    }

    @Override
    public String toString() {
        final var alias = alias();
        if (alias != null && !alias.isBlank()) {
            return alias;
        }
        assert !conditions.isEmpty();
        if (conditions.size() == 1) {
            final var condition0 = conditions.get(0);
            return condition0.toString();
        }
        final var delimiter = switch (operator) {
            case AND -> DELIMITER_AND;
            case OR -> DELIMITER_OR;
        };
        final var joiner = new StringJoiner(delimiter, PREFIX, SUFFIX);
        for (var condition : conditions) {
            joiner.add(condition.toString());
        }
        return joiner.toString();
    }
}
