/*
 * Copyright 2023 LINE Corporation
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

package com.linecorp.conditional.kotlin

import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

typealias CoroutineConditionFunction = suspend (CoroutineConditionContext) -> Boolean

abstract class CoroutineCondition(
    val alias: String? = DEFAULT_ALIAS,
    val delayMillis: Long = DEFAULT_DELAY_MILLIS,
    val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) {
    init {
        if (delayMillis < 0L) throw IllegalArgumentException("delayMillis: $delayMillis (expected >= 0)")
        if (timeoutMillis <= 0L) throw IllegalArgumentException("timeoutMillis: $timeoutMillis (expected > 0)")
    }

    /**
     * Returns a newly created [CoroutineCondition] with [alias] updated.
     */
    fun alias(block: () -> String?) = attributes { it.alias(block()) }

    /**
     * Returns a newly created [CoroutineCondition] with [delayMillis] updated.
     */
    fun delayMillis(block: () -> Long) = attributes { it.delayMillis(block()) }

    /**
     * Returns a newly created [CoroutineCondition] with [delayMillis] updated.
     */
    fun delay(block: () -> Duration) = attributes { it.delay(block()) }

    /**
     * Returns a newly created [CoroutineCondition] with [timeoutMillis] updated.
     */
    fun timeoutMillis(block: () -> Long) = attributes { it.timeoutMillis(block()) }

    /**
     * Returns a newly created [CoroutineCondition] with [timeoutMillis] updated.
     */
    fun timeout(block: () -> Duration) = attributes { it.timeout(block()) }

    private fun attributes(block: (AttributeUpdater) -> Unit) =
        with(AttributeUpdater(this)) { block(this).run { update() } }

    /**
     * Returns a newly created [CoroutineCondition].
     * This [CoroutineCondition] is composed with the AND operator.
     *
     * @param condition the [CoroutineCondition] to compose.
     */
    fun and(condition: CoroutineCondition) = composeWith(CoroutineConditionOperator.AND, condition)

    /**
     * Returns a newly created [CoroutineCondition].
     * This [CoroutineCondition] is composed with the OR operator.
     *
     * @param condition the [CoroutineCondition] to compose.
     */
    fun or(condition: CoroutineCondition) = composeWith(CoroutineConditionOperator.OR, condition)

    private fun composeWith(
        operator: CoroutineConditionOperator,
        condition: CoroutineCondition,
    ): CoroutineCondition = ComposedCoroutineCondition(operator, listOf(this, condition))

    /**
     * Returns a newly created negative [CoroutineCondition].
     */
    fun negate() = coroutineCondition("!${this.alias}") { !this.matches(it) }

    /**
     * Returns the match result of the [CoroutineCondition].
     *
     * @param ctx the context for matching [CoroutineCondition].
     *
     * @throws IllegalStateException if the [delayMillis] is greater than or equal to [timeoutMillis].
     * @throws TimeoutCancellationException if the timeout is exceeded.
     * @throws CancellationException if the [CoroutineCondition] is cancelled.
     */
    suspend fun matches(ctx: CoroutineConditionContext): Boolean {
        val thread = Thread.currentThread()
        val startTimeMillis = System.currentTimeMillis()
        val matches = try {
            assert(delayMillis >= 0 && timeoutMillis > 0)
            if (delayMillis >= timeoutMillis)
                throw IllegalStateException("delayMillis >= timeoutMillis (expected delayMillis < timeoutMillis)")
            if (delayMillis > 0) delay(delayMillis)
            withTimeout(timeoutMillis) { async { match(ctx) }.await() }
        } catch (e: Exception) {
            when (e) {
                is TimeoutCancellationException -> CoroutineConditionMatchState.TIMED_OUT
                is CancellationException -> CoroutineConditionMatchState.CANCELLED
                else -> CoroutineConditionMatchState.FAILED
            }.let {
                CoroutineConditionMatchResult(
                    thread, currentCoroutineContext(), this, it,
                    null, e, startTimeMillis, System.currentTimeMillis()
                )
            }.also(ctx::log)
            throw e
        }
        CoroutineConditionMatchResult(
            thread, currentCoroutineContext(), this, CoroutineConditionMatchState.COMPLETED,
            matches, null, startTimeMillis, System.currentTimeMillis()
        ).also(ctx::log)
        return matches
    }

    /**
     * Returns the match result of the [CoroutineCondition].
     *
     * @param ctx the context for matching [CoroutineCondition].
     *
     * @throws IllegalStateException if the [delayMillis] is greater than or equal to [timeoutMillis].
     * @throws TimeoutCancellationException if the timeout is exceeded.
     * @throws CancellationException if the [CoroutineCondition] is cancelled.
     * @see [runBlocking]
     * @see [CoroutineCondition.matches]
     */
    fun blockingMatches(ctx: CoroutineConditionContext): Boolean = runBlocking { matches(ctx) }

    protected abstract suspend fun match(ctx: CoroutineConditionContext): Boolean

    override fun toString(): String = if (!alias.isNullOrBlank()) alias else classNameOf(this, "Undefined")
    private fun classNameOf(obj: Any, defaultValue: String) = obj::class.simpleName ?: defaultValue

    companion object {
        @JvmStatic
        val DEFAULT_ALIAS: String? = null

        @JvmStatic
        val DEFAULT_DELAY_MILLIS: Long = 0L

        @JvmStatic
        val DEFAULT_TIMEOUT_MILLIS: Long = Long.MAX_VALUE
    }

    internal class AttributeUpdater internal constructor(condition: CoroutineCondition) {
        private val function: CoroutineConditionFunction

        @Volatile
        private var alias: String?

        @Volatile
        private var delayMillis: Long

        @Volatile
        private var timeoutMillis: Long

        init {
            function = condition::match
            alias = condition.alias
            delayMillis = condition.delayMillis
            timeoutMillis = condition.timeoutMillis
        }

        /**
         * Returns the [AttributeUpdater] with [alias] updated.
         */
        internal fun alias(alias: String?) = also { this.alias = alias }

        /**
         * Returns the [AttributeUpdater] with [delayMillis] updated.
         */
        internal fun delayMillis(delayMillis: Long) = also { this.delayMillis = delayMillis }

        /**
         * Returns the [AttributeUpdater] with [delayMillis] updated.
         */
        internal fun delay(delay: Duration) = also { delayMillis = delay.toLong(DurationUnit.MILLISECONDS) }

        /**
         * Returns the [AttributeUpdater] with [timeoutMillis] updated.
         */
        internal fun timeoutMillis(timeoutMillis: Long) = also { this.timeoutMillis = timeoutMillis }

        /**
         * Returns the [AttributeUpdater] with [timeoutMillis] updated.
         */
        internal fun timeout(timeout: Duration) =
            also { timeoutMillis = timeout.toLong(DurationUnit.MILLISECONDS) }

        internal fun update() = object : CoroutineCondition(alias, delayMillis, timeoutMillis) {
            override suspend fun match(ctx: CoroutineConditionContext): Boolean = function(ctx)
        }
    }
}

/**
 * Returns a newly created [CoroutineCondition].
 *
 * @param alias the value to set alias for the [CoroutineCondition].
 * @param delayMillis the value to set delay for the [CoroutineCondition].
 * @param timeoutMillis the value to set timeout for the [CoroutineCondition].
 * @param function the function to match the conditional expression.
 */
fun coroutineCondition(
    alias: String? = CoroutineCondition.DEFAULT_ALIAS,
    delayMillis: Long = CoroutineCondition.DEFAULT_DELAY_MILLIS,
    timeoutMillis: Long = CoroutineCondition.DEFAULT_TIMEOUT_MILLIS,
    function: CoroutineConditionFunction,
) = object : CoroutineCondition(alias, delayMillis, timeoutMillis) {
    override suspend fun match(ctx: CoroutineConditionContext): Boolean = function(ctx)
}

/**
 * Returns a newly created [CoroutineCondition].
 * This [CoroutineCondition] is composed with the AND operator.
 *
 * @param condition the [CoroutineCondition] to compose.
 */
infix fun CoroutineCondition.and(condition: CoroutineCondition) = this.and(condition)

/**
 * Returns a newly created [CoroutineCondition].
 * This [CoroutineCondition] is composed with the OR operator.
 *
 * @param condition the [CoroutineCondition] to compose.
 */
infix fun CoroutineCondition.or(condition: CoroutineCondition) = this.or(condition)

/**
 * Returns a newly created negative [CoroutineCondition].
 */
operator fun CoroutineCondition.not() = this.negate()
