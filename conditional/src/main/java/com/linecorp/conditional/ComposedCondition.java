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
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class ComposedCondition extends Condition {

    private static final String PREFIX = "(";
    private static final String SUFFIX = ")";
    private static final String DELIMITER_AND = " and ";
    private static final String DELIMITER_OR = " or ";

    private final Operator operator;
    private final List<Condition> conditions = new ArrayList<>();

    ComposedCondition(Operator operator, Condition... conditions) {
        this(operator, List.of(conditions));
    }

    ComposedCondition(Operator operator, List<Condition> conditions) {
        checkConstructorArguments(operator, conditions);
        this.operator = operator;
        this.conditions.addAll(conditions);
    }

    ComposedCondition(@Nullable String alias, boolean async, @Nullable Executor executor,
                      long delayMillis, long timeoutMillis, boolean cancellable,
                      Operator operator, List<Condition> conditions) {
        super(alias, async, executor, delayMillis, timeoutMillis, cancellable);
        checkConstructorArguments(operator, conditions);
        this.operator = operator;
        this.conditions.addAll(conditions);
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
    protected ComposedConditionAttributeUpdater attributeUpdater() {
        return new ComposedConditionAttributeUpdater(this);
    }

    ComposedCondition update(ComposedConditionAttributeUpdaterConsumer attributeUpdaterConsumer) {
        requireNonNull(attributeUpdaterConsumer, "attributeUpdaterConsumer");
        final var attributeUpdater = attributeUpdater();
        attributeUpdaterConsumer.accept(attributeUpdater);
        return attributeUpdater.update();
    }

    Operator operator() {
        return operator;
    }

    List<Condition> conditions() {
        return conditions;
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
        for (var cf : cfs) {
            if (future.isDone()) {
                break;
            }
            cf.whenComplete((value, e) -> {
                if (e != null) {
                    completeExceptionally(future, e);
                    cancel(cfs);
                    return;
                }
                if (shortCircuit(operator, value)) {
                    complete(future, true);
                }
            });
        }
        if (!future.isDone()) {
            CompletableFuture.allOf(cfs.toArray(CompletableFuture[]::new))
                             .thenRun(() -> complete(future, false));
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
        if (!cancellable()) {
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
