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

import static com.linecorp.conditional.Condition.failed;
import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionTest {

    @Test
    void constructor() {
        final var ctx = ConditionContext.of("result", true);
        final var condition = new Condition() {
            @Override
            protected boolean match(ConditionContext ctx) {
                return ctx.var("result", Boolean.class);
            }
        };
        assertThat(condition.matches(ctx)).isTrue();
    }

    @Test
    void of() {
        final var ctx = ConditionContext.of("result", true);
        final var condition = Condition.of(ctx0 -> ctx0.var("result", Boolean.class));
        assertThat(condition.matches(ctx)).isTrue();
    }

    static Condition c(Supplier<Condition> supplier) {
        requireNonNull(supplier, "supplier");
        return supplier.get();
    }

    static Stream<Arguments> durations() {
        final Supplier<Condition> t1 = () -> Condition.delayed(ctx -> true, 1000, TimeUnit.MILLISECONDS)
                                                      .alias("t1");
        final Supplier<Condition> f1 = () -> Condition.delayed(ctx -> false, 1000, TimeUnit.MILLISECONDS)
                                                      .alias("f1");
        final Supplier<Condition> t2 = () -> Condition.delayed(ctx -> true, 2000, TimeUnit.MILLISECONDS)
                                                      .alias("t2");
        final Supplier<Condition> f2 = () -> Condition.delayed(ctx -> false, 2000, TimeUnit.MILLISECONDS)
                                                      .alias("f2");
        final Supplier<Condition> t3 = () -> Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS)
                                                      .alias("t3");
        final Supplier<Condition> f3 = () -> Condition.delayed(ctx -> false, 3000, TimeUnit.MILLISECONDS)
                                                      .alias("f3");
        return Stream.of(
                // AND
                Arguments.of(c(t1), 1000, 1500),
                Arguments.of(c(t1).async(), 1000, 1500),
                Arguments.of(c(t2), 2000, 2500),
                Arguments.of(c(t2).async(), 2000, 2500),
                Arguments.of(c(t3), 3000, 3500),
                Arguments.of(c(t3).async(), 3000, 3500),
                Arguments.of(c(t2).and(c(t1)), 3000, 3500),
                Arguments.of(c(t2).async().and(c(t1)), 2000, 2500),
                Arguments.of(c(t2).and(c(t1).async()), 3000, 3500),
                Arguments.of(c(t2).async().and(c(t1).async()), 2000, 2500),
                Arguments.of(c(t2).and(c(t1)).async(), 3000, 3500),
                Arguments.of(c(t2).and(c(t1)).and(c(t3)), 6000, 6500),
                Arguments.of(c(t2).and(c(t1).async()).and(c(t3)), 5000, 5500),
                Arguments.of(c(t2).async().and(c(t1)).and(c(t3).async()), 4000, 4500),
                Arguments.of(c(t2).async().and(c(t1).async()).and(c(t3).async()), 3000, 3500),
                Arguments.of(c(t2).and(c(t1)).async().and(c(t3)), 3000, 3500),

                // OR
                Arguments.of(c(f1), 1000, 1500),
                Arguments.of(c(f1).async(), 1000, 1500),
                Arguments.of(c(f2), 2000, 2500),
                Arguments.of(c(f2).async(), 2000, 2500),
                Arguments.of(c(f3), 3000, 3500),
                Arguments.of(c(f3).async(), 3000, 3500),
                Arguments.of(c(f2).or(c(f1)), 3000, 3500),
                Arguments.of(c(f2).async().or(c(f1)), 2000, 2500),
                Arguments.of(c(f2).or(c(f1).async()), 3000, 3500),
                Arguments.of(c(f2).async().or(c(f1).async()), 2000, 2500),
                Arguments.of(c(f2).or(c(f1)).async(), 3000, 3500),
                Arguments.of(c(f2).or(c(f1)).or(c(f3)), 6000, 6500),
                Arguments.of(c(f2).or(c(f1).async()).or(c(f3)), 5000, 5500),
                Arguments.of(c(f2).async().or(c(f1)).or(c(f3).async()), 4000, 4500),
                Arguments.of(c(f2).async().or(c(f1).async()).or(c(f3).async()), 3000, 3500),
                Arguments.of(c(f2).or(c(f1)).async().or(c(t3)), 3000, 3500),

                // MIXIN
                Arguments.of(c(t2).or(c(t1)).and(c(t3)), 5000, 5500),
                Arguments.of(c(t2).async().or(c(t1)).and(c(t3)), 4000, 4500),
                Arguments.of(c(t2).async().or(c(f1)).and(c(t3)), 5000, 5500),
                Arguments.of(c(t2).async().or(c(t1).async()).and(c(t3)), 4000, 4500),
                Arguments.of(c(t2).async().or(c(t1).async()).and(c(t3).async()), 4000, 4500),
                Arguments.of(c(t2).async().or(c(f1).async()).and(c(t3)), 5000, 5500),
                Arguments.of(c(t2).async().or(c(f1).async()).and(c(t3).async()), 5000, 5500)
        );
    }

    @ParameterizedTest
    @MethodSource("durations")
    void duration(Condition condition, long atLeastMillis, long atMostMillis) {
        await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   final var ctx = ConditionContext.of();
                   assertThatCode(() -> condition.matches(ctx)).doesNotThrowAnyException();
                   return true;
               });
    }

    static Stream<Arguments> timeouts() {
        final BiFunction<Long, TimeUnit, Supplier<Condition>> f = (delay, unit) -> () -> Condition.of(ctx -> {
            try {
                Thread.sleep(unit.toMillis(delay));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        }).alias("Delayed(" + delay + ", " + unit + ')');
        final Supplier<Condition> t1 = f.apply(1000L, TimeUnit.MILLISECONDS);
        final Supplier<Condition> t2 = f.apply(2000L, TimeUnit.MILLISECONDS);
        return Stream.of(
                Arguments.of(c(t1), false, 1000, 1300),
                Arguments.of(c(t1).timeout(500), true, 500, 800),
                Arguments.of(c(t1).timeout(1500), false, 1000, 1300),

                Arguments.of(c(t1).and(c(t2)), false, 3000, 3300),
                Arguments.of(c(t1).and(c(t2)).timeout(2500), true, 2500, 2800),
                Arguments.of(c(t1).and(c(t2)).timeout(3500), false, 3000, 3300),
                Arguments.of(c(t1).timeout(500).and(c(t2)), true, 500, 800),
                Arguments.of(c(t1).timeout(1500).and(c(t2)), false, 3000, 3300),
                Arguments.of(c(t1).and(c(t2).timeout(1500)), true, 2500, 2800),
                Arguments.of(c(t1).and(c(t2).timeout(2500)), false, 3000, 3300),

                Arguments.of(c(t1).or(c(t2)), false, 1000, 1300),
                Arguments.of(c(t1).or(c(t2)).timeout(500), true, 500, 800),
                Arguments.of(c(t1).or(c(t2)).timeout(1500), false, 1000, 1300),
                Arguments.of(c(t1).timeout(500).or(c(t2)), true, 500, 800),
                Arguments.of(c(t1).timeout(1500).or(c(t2)), false, 1000, 1300),
                Arguments.of(c(t1).or(c(t2).timeout(500)), false, 1000, 1300),
                Arguments.of(c(t1).or(c(t2).timeout(1500)), false, 1000, 1300));
    }

    @ParameterizedTest
    @MethodSource("timeouts")
    void timeout(Condition condition, boolean timeout, long atLeastDurationMillis, long atMostDurationMillis) {
        await().atLeast(atLeastDurationMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostDurationMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   final var ctx = ConditionContext.of();
                   try {
                       condition.matches(ctx);
                       if (timeout) {
                           fail("If timeout is true, this code should not run.");
                       }
                   } catch (Exception e) {
                       if (timeout) {
                           var raised = false;
                           for (Throwable t = e; t != null; t = t.getCause()) {
                               if (t instanceof TimeoutException) {
                                   raised = true;
                                   break;
                               }
                           }
                           assertThat(raised).isTrue();
                       } else {
                           fail("If timeout is false, this code should not run.", e);
                       }
                   }
                   return true;
               });
    }

    @Test
    void sync() {
        final var ctx = ConditionContext.of("a", true, "b", true);
        final var a = Condition.of(ctx0 -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ctx0.var("a", Boolean.class);
        }).alias("a");
        final var b = Condition.of(ctx0 -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ctx0.var("b", Boolean.class);
        }).alias("b");
        final var condition = a.and(b);
        await().atLeast(7000, TimeUnit.MILLISECONDS)
               .atMost(8000, TimeUnit.MILLISECONDS)
               .until(() -> {
                   condition.matches(ctx);
                   return true;
               });

        final var matchResults = ctx.logs();
        assertThat(matchResults.size()).isEqualTo(3);
        assertDurationMillis(matchResults.get(0), 3000, 4000);
        assertDurationMillis(matchResults.get(1), 4000, 5000);
        assertDurationMillis(matchResults.get(2), 7000, 8000);
    }

    @Test
    void async() {
        final var ctx = ConditionContext.of("a", true, "b", true);
        final var a = Condition.of(ctx0 -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ctx0.var("a", Boolean.class);
        }).alias("a").async();
        final var b = Condition.of(ctx0 -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return ctx0.var("b", Boolean.class);
        }).alias("b").async();
        final var condition = a.and(b);
        await().atLeast(4000, TimeUnit.MILLISECONDS)
               .atMost(5000, TimeUnit.MILLISECONDS)
               .until(() -> {
                   condition.matches(ctx);
                   return true;
               });

        final var matchResults = ctx.logs();
        assertThat(matchResults.size()).isEqualTo(3);
        assertDurationMillis(matchResults.get(0), 3000, 4000);
        assertDurationMillis(matchResults.get(1), 4000, 5000);
        assertDurationMillis(matchResults.get(2), 4000, 5000);
    }

    @Test
    void completed() {
        assertThat(Condition.completed(true).matches(ConditionContext.of())).isTrue();
        assertThat(Condition.completed(false).matches(ConditionContext.of())).isFalse();
    }

    static Stream<Arguments> SEQUENTIAL() {
        final var a = Condition.delayed(ctx -> true, 2000, TimeUnit.MILLISECONDS).alias("a");
        final var b = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).alias("b");
        return Stream.of(
                Arguments.of(a.and(b).sequential(), 5000, 5500),
                Arguments.of(a.or(b).sequential(), 2000, 2500));
    }

    @ParameterizedTest
    @MethodSource("SEQUENTIAL")
    void sequential(Condition condition, long atLeastMillis, long atMostMillis) {
        final var ctx = ConditionContext.of();
        await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   assertThat(condition.matches(ctx)).isTrue();
                   return true;
               });
    }

    static Stream<Arguments> PARALLEL() {
        final var a = Condition.delayed(ctx -> true, 2000, TimeUnit.MILLISECONDS).alias("a");
        final var b = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).alias("b");
        final var executor = Executors.newSingleThreadExecutor();
        return Stream.of(
                Arguments.of(a.and(b).parallel(), 3000, 3500),
                Arguments.of(a.or(b).parallel(), 2000, 2500),
                Arguments.of(a.and(b).parallel(executor), 5000, 5500),
                Arguments.of(a.or(b).parallel(executor), 2000, 3500));
    }

    @ParameterizedTest
    @MethodSource("PARALLEL")
    void parallel(Condition condition, long atLeastMillis, long atMostMillis) {
        final var ctx = ConditionContext.of();
        await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
               .atMost(atMostMillis, TimeUnit.MILLISECONDS)
               .until(() -> {
                   assertThat(condition.matches(ctx)).isTrue();
                   return true;
               });
    }

    @Nested
    class MatchesTest {

        static Stream<Arguments> AND() {
            return Stream.of(
                    Arguments.of(trueCondition(), trueCondition(), true),
                    Arguments.of(trueCondition().async(), trueCondition(), true),
                    Arguments.of(trueCondition(), trueCondition().async(), true),
                    Arguments.of(trueCondition().async(), trueCondition().async(), true),

                    Arguments.of(trueCondition(), falseCondition(), false),
                    Arguments.of(trueCondition().async(), falseCondition(), false),
                    Arguments.of(trueCondition(), falseCondition().async(), false),
                    Arguments.of(trueCondition().async(), falseCondition().async(), false),

                    Arguments.of(falseCondition(), trueCondition(), false),
                    Arguments.of(falseCondition().async(), trueCondition(), false),
                    Arguments.of(falseCondition(), trueCondition().async(), false),
                    Arguments.of(falseCondition().async(), trueCondition().async(), false),

                    Arguments.of(falseCondition(), falseCondition(), false),
                    Arguments.of(falseCondition().async(), falseCondition(), false),
                    Arguments.of(falseCondition(), falseCondition().async(), false),
                    Arguments.of(falseCondition().async(), falseCondition().async(), false));
        }

        @ParameterizedTest
        @MethodSource("AND")
        void AND_with_composer(Condition left, Condition right, boolean expected) {
            final var conditionComposer = Condition.composer(Operator.AND);
            final var condition = conditionComposer.with(left, right).compose();
            assertThat(condition.matches(ConditionContext.of())).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("AND")
        void AND_with_chaining(Condition left, Condition right, boolean expected) {
            final var ctx = ConditionContext.of();
            final var condition = left.and(right);
            assertThat(condition.matches(ctx)).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("AND")
        void allOf(Condition left, Condition right, boolean expected) {
            final var allOf = Condition.allOf(left, right);
            assertThat(allOf.matches(ConditionContext.of())).isEqualTo(expected);
        }

        static Stream<Arguments> OR() {
            return Stream.of(
                    Arguments.of(trueCondition(), trueCondition(), true),
                    Arguments.of(trueCondition().async(), trueCondition(), true),
                    Arguments.of(trueCondition(), trueCondition().async(), true),
                    Arguments.of(trueCondition().async(), trueCondition().async(), true),

                    Arguments.of(trueCondition(), falseCondition(), true),
                    Arguments.of(trueCondition().async(), falseCondition(), true),
                    Arguments.of(trueCondition(), falseCondition().async(), true),
                    Arguments.of(trueCondition().async(), falseCondition().async(), true),

                    Arguments.of(falseCondition(), trueCondition(), true),
                    Arguments.of(falseCondition().async(), trueCondition(), true),
                    Arguments.of(falseCondition(), trueCondition().async(), true),
                    Arguments.of(falseCondition().async(), trueCondition().async(), true),

                    Arguments.of(falseCondition(), falseCondition(), false),
                    Arguments.of(falseCondition().async(), falseCondition(), false),
                    Arguments.of(falseCondition(), falseCondition().async(), false),
                    Arguments.of(falseCondition().async(), falseCondition().async(), false));
        }

        @ParameterizedTest
        @MethodSource("OR")
        void OR_with_composer(Condition left, Condition right, boolean expected) {
            final var conditionComposer = Condition.composer(Operator.OR);
            final var condition = conditionComposer.with(left, right).compose();
            assertThat(condition.matches(ConditionContext.of())).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("OR")
        void OR_with_chaining(Condition left, Condition right, boolean expected) {
            assertThat(left.or(right).matches(ConditionContext.of())).isEqualTo(expected);
        }

        @ParameterizedTest
        @MethodSource("OR")
        void anyOf(Condition left, Condition right, boolean expected) {
            final var anyOf = Condition.anyOf(left, right);
            assertThat(anyOf.matches(ConditionContext.of())).isEqualTo(expected);
        }

        static Stream<Arguments> NOR() {
            return Stream.of(
                    Arguments.of(trueCondition(), trueCondition(), false),
                    Arguments.of(trueCondition().async(), trueCondition(), false),
                    Arguments.of(trueCondition(), trueCondition().async(), false),
                    Arguments.of(trueCondition().async(), trueCondition().async(), false),

                    Arguments.of(trueCondition(), falseCondition(), false),
                    Arguments.of(trueCondition().async(), falseCondition(), false),
                    Arguments.of(trueCondition(), falseCondition().async(), false),
                    Arguments.of(trueCondition().async(), falseCondition().async(), false),

                    Arguments.of(falseCondition(), trueCondition(), false),
                    Arguments.of(falseCondition().async(), trueCondition(), false),
                    Arguments.of(falseCondition(), trueCondition().async(), false),
                    Arguments.of(falseCondition().async(), trueCondition().async(), false),

                    Arguments.of(falseCondition(), falseCondition(), true),
                    Arguments.of(falseCondition().async(), falseCondition(), true),
                    Arguments.of(falseCondition(), falseCondition().async(), true),
                    Arguments.of(falseCondition().async(), falseCondition().async(), true));
        }

        @ParameterizedTest
        @MethodSource("NOR")
        void noneOf(Condition left, Condition right, boolean expected) {
            final var noneOf = Condition.noneOf(left, right);
            assertThat(noneOf.matches(ConditionContext.of())).isEqualTo(expected);
        }

        @Test
        void failed_with_Throwable() {
            final var ctx = ConditionContext.of();
            assertThatThrownBy(() -> failed(new RuntimeException()).matches(ctx))
                    .isExactlyInstanceOf(RuntimeException.class);
        }

        @Test
        void failed_with_Supplier() {
            final var ctx = ConditionContext.of();
            assertThatThrownBy(() -> failed(() -> new RuntimeException()).matches(ctx))
                    .isExactlyInstanceOf(RuntimeException.class);
        }

        @Test
        void failed_with_ContextAwareSupplier() {
            final var ctx = ConditionContext.of();
            assertThatThrownBy(() -> {
                failed(ctx0 -> {
                    assertThat(ctx0).isSameAs(ctx);
                    return new RuntimeException();
                }).matches(ctx);
            }).isExactlyInstanceOf(RuntimeException.class);
        }

        static Stream<Arguments> precedence() {
            return Stream.of(
                    // ((true or false) and false) = false
                    Arguments.of(trueCondition().or(falseCondition()).and(falseCondition()), false),

                    // (true or (false and false)) = true
                    Arguments.of(trueCondition().or(falseCondition().and(falseCondition())), true));
        }

        @ParameterizedTest
        @MethodSource("precedence")
        void precedence(Condition condition, boolean expected) {
            assertThat(condition.matches(ConditionContext.of())).isEqualTo(expected);
        }

        @Test
        void delayed() {
            final var ctx = ConditionContext.of("a", true, "b", true);
            final var a = Condition.of(ctx0 -> {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return ctx0.var("a", Boolean.class);
            }).alias("a");
            final var b = Condition.of(ctx0 -> {
                try {
                    Thread.sleep(4000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return ctx0.var("b", Boolean.class);
            }).alias("b");
            final var condition = a.and(b);
            await().atLeast(Duration.ofMillis(7000L))
                   .atMost(Duration.ofMillis(8000L))
                   .until(() -> {
                       assertThat(condition.matches(ctx)).isTrue();
                       return true;
                   });
        }
    }

    @Nested
    class ToStringTest {

        static Stream<Arguments> someConditions() {
            return Stream.of(
                    Arguments.of(Condition.of(ctx -> true), "Undefined"),
                    Arguments.of(Condition.of(ctx -> false), "Undefined"),
                    Arguments.of(Condition.of(ctx -> {
                        throw new RuntimeException();
                    }), "Undefined"),
                    Arguments.of(failed(unused -> new RuntimeException()), "FailedCondition"),
                    Arguments.of(trueCondition(), "TrueCondition"),
                    Arguments.of(falseCondition(), "FalseCondition"),
                    Arguments.of(Condition.composer(Operator.AND).with(trueCondition()).compose(),
                                 "TrueCondition"),
                    Arguments.of(Condition.composer(Operator.OR).with(trueCondition()).compose(),
                                 "TrueCondition"),
                    Arguments.of(trueCondition().and(falseCondition()),
                                 "(TrueCondition and FalseCondition)"),
                    Arguments.of(trueCondition().or(falseCondition()),
                                 "(TrueCondition or FalseCondition)"),
                    Arguments.of(trueCondition().and(falseCondition()).and(trueCondition()),
                                 "(TrueCondition and FalseCondition and TrueCondition)"),
                    Arguments.of(trueCondition().or(falseCondition()).or(trueCondition()),
                                 "(TrueCondition or FalseCondition or TrueCondition)"),
                    Arguments.of(trueCondition().or(falseCondition().and(trueCondition())),
                                 "(TrueCondition or (FalseCondition and TrueCondition))"),
                    Arguments.of(trueCondition().or(falseCondition()).and(trueCondition()),
                                 "((TrueCondition or FalseCondition) and TrueCondition)"),
                    Arguments.of(trueCondition().and(falseCondition()).async().and(trueCondition()),
                                 "((TrueCondition and FalseCondition) and TrueCondition)"),
                    Arguments.of(trueCondition().async().and(falseCondition()).and(trueCondition()),
                                 "(TrueCondition and FalseCondition and TrueCondition)"),
                    Arguments.of(trueCondition().and(falseCondition()).and(trueCondition().async()),
                                 "(TrueCondition and FalseCondition and TrueCondition)"));
        }

        @ParameterizedTest
        @MethodSource("someConditions")
        void someConditions(Condition condition, String expected) {
            assertThat(condition.toString()).isEqualTo(expected);
        }
    }

    static void assertDurationMillis(ConditionMatchResult conditionMatchResult,
                                     long atLeastMillisInclusive, long atMostMillisExclusive) {
        requireNonNull(conditionMatchResult, "conditionMatchResult");
        final var durationMillis = conditionMatchResult.durationMillis();
        assertThat(atLeastMillisInclusive <= durationMillis && durationMillis < atMostMillisExclusive).isTrue();
    }
}
