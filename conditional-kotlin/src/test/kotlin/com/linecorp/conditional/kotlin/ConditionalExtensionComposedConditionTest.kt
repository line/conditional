package com.linecorp.conditional.kotlin

import com.linecorp.conditional.Condition
import com.linecorp.conditional.ConditionContext
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert.ThrowingCallable
import org.awaitility.Awaitility
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

internal class ConditionalExtensionComposedConditionTest {

    companion object {
        private val trueCondition = Condition.trueCondition()
        private val falseCondition = Condition.falseCondition()
        private val failed = Condition.failed { _: ConditionContext? -> RuntimeException() }

        @JvmStatic
        private fun AND(): Stream<Arguments?>? {
            return Stream.of( // true and failed = exception raised
                Arguments.of(
                    trueCondition and failed,
                    RuntimeException()::class.java, null
                ),  // false and failed = false
                Arguments.of(
                    falseCondition and failed,
                    null, false
                ),  // false and (failed and true) = false
                Arguments.of(
                    falseCondition and (failed and trueCondition),
                    null, false
                ),  // (true and true) and failed = exception raised
                Arguments.of(
                    (trueCondition and trueCondition) and failed,
                    RuntimeException()::class.java, null
                ),  // (true and false) and failed = false
                Arguments.of(
                    (trueCondition and falseCondition) and failed,
                    null, false
                )
            )
        }

        @JvmStatic
        private fun OR(): Stream<Arguments?>? {
            return Stream.of( // false or failed = exception raised
                Arguments.of(
                    falseCondition or failed,
                    RuntimeException()::class.java, null
                ),  // true or failed = true
                Arguments.of(
                    trueCondition or failed,
                    null, true
                ),  // true or (failed or false) = true
                Arguments.of(
                    trueCondition or (failed or falseCondition),
                    null, true
                ),  // (true or false) or failed = true
                Arguments.of(
                    (trueCondition or falseCondition) or failed,
                    null, true
                ),  // (false or false) or failed = exception raised
                Arguments.of(
                    (falseCondition or falseCondition) or failed,
                    RuntimeException()::class.java, null
                )
            )
        }

        @JvmStatic
        private fun SEQUENTIAL(): Stream<Arguments?>? {
            val a = Condition.delayed({ _: ConditionContext? -> true }, 2000, TimeUnit.MILLISECONDS).alias("a")
            val b = Condition.delayed({ _: ConditionContext? -> true }, 3000, TimeUnit.MILLISECONDS).alias("b")
            return Stream.of(
                Arguments.of((a and b).sequential(), 5000, 5500),
                Arguments.of((a or b).sequential(), 2000, 2500)
            )
        }

        @JvmStatic
        private fun PARALLEL(): Stream<Arguments?>? {
            val a = Condition.delayed({ _: ConditionContext? -> true }, 2000, TimeUnit.MILLISECONDS).alias("a")
            val b = Condition.delayed({ _: ConditionContext? -> true }, 3000, TimeUnit.MILLISECONDS).alias("b")
            val executor = Executors.newSingleThreadExecutor()
            return Stream.of(
                Arguments.of((a and b).parallel(), 3000, 3500),
                Arguments.of((a or b).parallel(), 2000, 2500),
                Arguments.of((a and b).parallel(executor), 5000, 5500),
                Arguments.of((a or b).parallel(executor), 2000, 3500)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("AND")
    @Throws(Throwable::class)
    fun matches_when_operator_AND(
        condition: Condition,
        expectedException: Class<out Throwable?>?,
        expectedMatches: Boolean?,
    ) {
        val ctx = ConditionContext.of()
        val throwingCallable = ThrowingCallable { condition.matches(ctx) }
        if (expectedException != null) {
            Assertions.assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(expectedException)
        } else {
            Objects.requireNonNull(expectedMatches, "expectedMatches")
            throwingCallable.call()
        }
    }

    @ParameterizedTest
    @MethodSource("OR")
    @Throws(Throwable::class)
    fun matches_when_operator_OR(
        condition: Condition,
        expectedException: Class<out Throwable?>?,
        expectedMatches: Boolean?,
    ) {
        val ctx = ConditionContext.of()
        val throwingCallable = ThrowingCallable { condition.matches(ctx) }
        if (expectedException != null) {
            Assertions.assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(expectedException)
        } else {
            Objects.requireNonNull(expectedMatches, "expectedMatches")
            throwingCallable.call()
        }
    }

    @ParameterizedTest
    @MethodSource("SEQUENTIAL")
    fun sequential(condition: Condition, atLeastMillis: Long, atMostMillis: Long) {
        val ctx = ConditionContext.of()
        Awaitility.await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
            .atMost(atMostMillis, TimeUnit.MILLISECONDS)
            .until {
                Assertions.assertThat(condition.matches(ctx)).isTrue
                true
            }
    }

    @ParameterizedTest
    @MethodSource("PARALLEL")
    fun parallel(condition: Condition, atLeastMillis: Long, atMostMillis: Long) {
        val ctx = ConditionContext.of()
        Awaitility.await().atLeast(atLeastMillis, TimeUnit.MILLISECONDS)
            .atMost(atMostMillis, TimeUnit.MILLISECONDS)
            .until {
                Assertions.assertThat(condition.matches(ctx)).isTrue
                true
            }
    }
}
