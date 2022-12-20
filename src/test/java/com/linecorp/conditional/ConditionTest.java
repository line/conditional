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
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Map;
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
        assertTrue(condition.matches(ctx));
    }

    @Test
    void of() {
        final var ctx = ConditionContext.of("result", true);
        final var condition = Condition.of(ctx0 -> ctx0.var("result", Boolean.class));
        assertTrue(condition.matches(ctx));
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
                   assertDoesNotThrow(() -> condition.matches(ctx));
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
                           fail();
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
                           assertTrue(raised);
                       } else {
                           fail(e);
                       }
                   }
                   return true;
               });
    }

    @Test
    void sync() {
        final var ctx = ConditionContext.of(Map.of("a", true, "b", true));
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

        final var executionResults = ctx.conditionExecutionResults();
        assertEquals(3, executionResults.size());
        assertDurationMillis(executionResults.get(0), 3000, 4000);
        assertDurationMillis(executionResults.get(1), 4000, 5000);
        assertDurationMillis(executionResults.get(2), 7000, 8000);
    }

    @Test
    void async() {
        final var ctx = ConditionContext.of(Map.of("a", true, "b", true));
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

        final var executionResults = ctx.conditionExecutionResults();
        assertEquals(3, executionResults.size());
        assertDurationMillis(executionResults.get(0), 3000, 4000);
        assertDurationMillis(executionResults.get(1), 4000, 5000);
        assertDurationMillis(executionResults.get(2), 4000, 5000);
    }

    @Test
    void completed() {
        final var ctx = ConditionContext.of();
        assertTrue(Condition.completed(true).matches(ctx));
        assertFalse(Condition.completed(false).matches(ctx));
    }

    static Stream<Arguments> deadlocks() {
        final var executor = Executors.newSingleThreadExecutor();
        final Supplier<Condition> async = () -> Condition.async(ctx -> true);
        final Supplier<Condition> sync = () -> Condition.of(ctx -> true);
        return Stream.of(
                Arguments.of(c(async).executor(executor).and(c(sync)).executor(executor)),
                Arguments.of(c(async).and(c(sync).executor(executor)).executor(executor)));
    }

    @ParameterizedTest
    @MethodSource("deadlocks")
    void avoid_deadlock_when_matches(Condition condition) {
        await().atMost(1000, TimeUnit.MILLISECONDS)
               .until(() -> {
                   final var ctx = ConditionContext.of();
                   condition.matches(ctx);
                   return true;
               });
    }

    @Nested
    class MatchesTest {

        static Stream<Arguments> AND() {
            return Stream.of(
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition(), true),
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition().async(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition().async(), true),

                    Arguments.of(Condition.trueCondition(), Condition.falseCondition(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition(), false),
                    Arguments.of(Condition.trueCondition(), Condition.falseCondition().async(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition().async(), false),

                    Arguments.of(Condition.falseCondition(), Condition.trueCondition(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition(), false),
                    Arguments.of(Condition.falseCondition(), Condition.trueCondition().async(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition().async(), false),

                    Arguments.of(Condition.falseCondition(), Condition.falseCondition(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition(), false),
                    Arguments.of(Condition.falseCondition(), Condition.falseCondition().async(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition().async(),
                                 false));
        }

        @ParameterizedTest
        @MethodSource("AND")
        void AND_with_composer(Condition left, Condition right, boolean expected) {
            final var conditionComposer = Condition.composer(Operator.AND);
            final var condition = conditionComposer.with(left, right).compose();
            assertEquals(expected, condition.matches(ConditionContext.of()));
        }

        @ParameterizedTest
        @MethodSource("AND")
        void AND_with_chaining(Condition left, Condition right, boolean expected) {
            final var ctx = ConditionContext.of();
            final var condition = left.and(right);
            assertEquals(expected, condition.matches(ctx));
        }

        @ParameterizedTest
        @MethodSource("AND")
        void allOf(Condition left, Condition right, boolean expected) {
            final var allOf = Condition.allOf(left, right);
            assertEquals(expected, allOf.matches(ConditionContext.of()));
        }

        static Stream<Arguments> OR() {
            return Stream.of(
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition(), true),
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition().async(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition().async(), true),

                    Arguments.of(Condition.trueCondition(), Condition.falseCondition(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition(), true),
                    Arguments.of(Condition.trueCondition(), Condition.falseCondition().async(), true),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition().async(), true),

                    Arguments.of(Condition.falseCondition(), Condition.trueCondition(), true),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition(), true),
                    Arguments.of(Condition.falseCondition(), Condition.trueCondition().async(), true),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition().async(), true),

                    Arguments.of(Condition.falseCondition(), Condition.falseCondition(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition(), false),
                    Arguments.of(Condition.falseCondition(), Condition.falseCondition().async(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition().async(),
                                 false));
        }

        @ParameterizedTest
        @MethodSource("OR")
        void OR_with_composer(Condition left, Condition right, boolean expected) {
            final var conditionComposer = Condition.composer(Operator.OR);
            final var condition = conditionComposer.with(left, right).compose();
            assertEquals(expected, condition.matches(ConditionContext.of()));
        }

        @ParameterizedTest
        @MethodSource("OR")
        void OR_with_chaining(Condition left, Condition right, boolean expected) {
            assertEquals(expected, left.or(right).matches(ConditionContext.of()));
        }

        @ParameterizedTest
        @MethodSource("OR")
        void anyOf(Condition left, Condition right, boolean expected) {
            final var anyOf = Condition.anyOf(left, right);
            assertEquals(expected, anyOf.matches(ConditionContext.of()));
        }

        static Stream<Arguments> NOR() {
            return Stream.of(
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition(), false),
                    Arguments.of(Condition.trueCondition(), Condition.trueCondition().async(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.trueCondition().async(), false),

                    Arguments.of(Condition.trueCondition(), Condition.falseCondition(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition(), false),
                    Arguments.of(Condition.trueCondition(), Condition.falseCondition().async(), false),
                    Arguments.of(Condition.trueCondition().async(), Condition.falseCondition().async(), false),

                    Arguments.of(Condition.falseCondition(), Condition.trueCondition(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition(), false),
                    Arguments.of(Condition.falseCondition(), Condition.trueCondition().async(), false),
                    Arguments.of(Condition.falseCondition().async(), Condition.trueCondition().async(), false),

                    Arguments.of(Condition.falseCondition(), Condition.falseCondition(), true),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition(), true),
                    Arguments.of(Condition.falseCondition(), Condition.falseCondition().async(), true),
                    Arguments.of(Condition.falseCondition().async(), Condition.falseCondition().async(), true));
        }

        @ParameterizedTest
        @MethodSource("NOR")
        void noneOf(Condition left, Condition right, boolean expected) {
            final var noneOf = Condition.noneOf(left, right);
            assertEquals(expected, noneOf.matches(ConditionContext.of()));
        }

        @Test
        void exceptional_with_ContextAwareSupplier() {
            final var ctx = ConditionContext.of();
            assertThrows(RuntimeException.class, () -> Condition.exceptional(ctx0 -> {
                assertSame(ctx, ctx0);
                return new RuntimeException();
            }).matches(ctx));
        }

        static Stream<Arguments> precedence() {
            return Stream.of(
                    // ((true || false) && false) = false
                    Arguments.of(Condition.trueCondition().or(Condition.falseCondition()).and(
                            Condition.falseCondition()), false),

                    // (true || (false && false)) = true
                    Arguments.of(
                            Condition.trueCondition()
                                     .or(Condition.falseCondition().and(Condition.falseCondition())), true));
        }

        @ParameterizedTest
        @MethodSource("precedence")
        void precedence(Condition condition, boolean expected) {
            assertEquals(expected, condition.matches(ConditionContext.of()));
        }

        @Test
        void delayed() {
            final var ctx = ConditionContext.of(Map.of("a", true, "b", true));
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
                       assertTrue(condition.matches(ctx));
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
                    Arguments.of(Condition.exceptional(unused -> new RuntimeException()),
                                 "ExceptionalCondition"),
                    Arguments.of(Condition.trueCondition(), "TrueCondition"),
                    Arguments.of(Condition.falseCondition(), "FalseCondition"),
                    Arguments.of(Condition.composer(Operator.AND).with(Condition.trueCondition()).compose(),
                                 "TrueCondition"),
                    Arguments.of(Condition.composer(Operator.OR).with(Condition.trueCondition()).compose(),
                                 "TrueCondition"),
                    Arguments.of(Condition.trueCondition().and(Condition.falseCondition()),
                                 "(TrueCondition && FalseCondition)"),
                    Arguments.of(Condition.trueCondition().or(Condition.falseCondition()),
                                 "(TrueCondition || FalseCondition)"),
                    Arguments.of(Condition.trueCondition().and(Condition.falseCondition()).and(
                                         Condition.trueCondition()),
                                 "(TrueCondition && FalseCondition && TrueCondition)"),
                    Arguments.of(
                            Condition.trueCondition().or(Condition.falseCondition())
                                     .or(Condition.trueCondition()),
                            "(TrueCondition || FalseCondition || TrueCondition)"),
                    Arguments.of(
                            Condition.trueCondition()
                                     .or(Condition.falseCondition().and(Condition.trueCondition())),
                            "(TrueCondition || (FalseCondition && TrueCondition))"),
                    Arguments.of(
                            Condition.trueCondition().or(Condition.falseCondition())
                                     .and(Condition.trueCondition()),
                            "((TrueCondition || FalseCondition) && TrueCondition)"),
                    Arguments.of(Condition.trueCondition().and(Condition.falseCondition()).async().and(
                                         Condition.trueCondition()),
                                 "((TrueCondition && FalseCondition) && TrueCondition)"),
                    Arguments.of(Condition.trueCondition().async().and(Condition.falseCondition()).and(
                                         Condition.trueCondition()),
                                 "(TrueCondition && FalseCondition && TrueCondition)"),
                    Arguments.of(Condition.trueCondition().and(Condition.falseCondition()).and(
                                         Condition.trueCondition().async()),
                                 "(TrueCondition && FalseCondition && TrueCondition)"));
        }

        @ParameterizedTest
        @MethodSource("someConditions")
        void someConditions(Condition condition, String expected) {
            assertEquals(expected, condition.toString());
        }
    }

    static void assertDurationMillis(ConditionExecutionResult executionResult,
                                     long atLeastMillisInclusive, long atMostMillisExclusive) {
        requireNonNull(executionResult, "executionResult");
        final var durationMillis = executionResult.durationMillis();
        assertTrue(atLeastMillisInclusive <= durationMillis && durationMillis < atMostMillisExclusive);
    }
}
