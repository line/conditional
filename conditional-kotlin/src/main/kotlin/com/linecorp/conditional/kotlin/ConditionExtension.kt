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

package com.linecorp.conditional.kotlin

import com.linecorp.conditional.*
import java.util.concurrent.Executor

/**
 * Returns a newly created [Condition].
 *
 * @param alias the value to set alias for the [Condition].
 * @param async the value to enable async for the [Condition].
 * @param executor the executor to match the [Condition].
 * @param delayMillis the value to set delay for the [Condition].
 * @param timeoutMillis the value to set timeout for the [Condition].
 * @param cancellable the value to enable cancellable for the [Condition].
 * @param function the function to match the conditional expression.
 */
fun condition(
    alias: String? = Condition.DEFAULT_ALIAS,
    async: Boolean = Condition.DEFAULT_ASYNC_ENABLED,
    executor: Executor? = Condition.DEFAULT_EXECUTOR,
    delayMillis: Long = Condition.DEFAULT_DELAY_MILLIS,
    timeoutMillis: Long = Condition.DEFAULT_TIMEOUT_MILLIS,
    cancellable: Boolean = Condition.DEFAULT_CANCELLABLE_ENABLED,
    function: ConditionFunction,
): Condition = Condition.of(function)
    .alias(alias).async(async).executor(executor)
    .delayMillis(delayMillis).timeoutMillis(timeoutMillis).cancellable(cancellable)

/**
 * Returns the [ConditionBuilder].
 */
fun conditionBuilder(): ConditionBuilder = Condition.builder()

/**
 * Returns the [ConditionComposer].
 *
 * @param operator the operator of [ComposedCondition].
 */
fun conditionComposer(operator: ConditionOperator): ConditionComposer = Condition.composer(operator)

/**
 * Returns a newly created [Condition].
 * This [Condition] is composed with the AND operator.
 *
 * @param condition the [Condition] to compose.
 */
infix fun Condition.and(condition: Condition): Condition = this.and(condition)

/**
 * Returns a newly created [Condition].
 * This [Condition] is composed with the OR operator.
 *
 * @param condition the [Condition] to compose.
 */
infix fun Condition.or(condition: Condition): Condition = this.or(condition)

/**
 * Returns a newly created negative [Condition].
 */
operator fun Condition.not(): Condition = Condition.not(this)

/**
 * Returns a newly created [ConditionContext] by [Map].
 */
fun conditionContext(contextVariables: Map<String, Any> = mapOf()): ConditionContext =
    ConditionContext.of(contextVariables)

/**
 * Returns a newly created [ConditionContext] by [Pair]s.
 */
fun conditionContext(vararg contextVariables: Pair<String, Any>): ConditionContext =
    conditionContext(mapOf(*contextVariables))

/**
 * Returns a newly created [ConditionContext] by [Map].
 */
fun Map<String, Any>.asConditionContext(): ConditionContext = conditionContext(this)

/**
 * Returns a newly created [ConditionContext] by [Pair].
 */
fun Pair<String, Any>.asConditionContext(): ConditionContext = conditionContext(this)

/**
 * Returns the value corresponding to the [key].
 */
operator fun ConditionContext.get(key: String): Any? = this.`var`(key)

/**
 * Returns a newly created [ConditionContextBuilder].
 */
fun conditionContextBuilder(): ConditionContextBuilder = ConditionContext.builder()
