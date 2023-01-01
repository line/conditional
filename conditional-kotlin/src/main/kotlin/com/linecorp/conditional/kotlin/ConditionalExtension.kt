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
import java.util.concurrent.TimeUnit

/**
 * Returns a newly created [ComposedCondition].
 * This [ComposedCondition] is composed with the AND operator.
 *
 * @param condition the [Condition] to compose.
 *
 * @throws NullPointerException if the [condition] is null.
 */
infix fun Condition.and(condition: Condition): ComposedCondition = this.and(condition)

/**
 * Returns a newly created [ComposedCondition].
 * This [ComposedCondition] is composed with the OR operator.
 *
 * @param condition the [Condition] to compose.
 *
 * @throws NullPointerException if the [condition] is null.
 */
infix fun Condition.or(condition: Condition): ComposedCondition = this.or(condition)

/**
 * Returns a newly created negative [Condition].
 *
 * @throws NullPointerException if the [condition] is null.
 */
operator fun Condition.not(): Condition = Condition.not(this)

/**
 * Returns a newly created [Condition].
 *
 * @param function the function to match the conditional expression.
 *
 * @throws NullPointerException if the [function] is null.
 */
fun condition(function: ConditionFunction): Condition = Condition.of(function)

/**
 * Returns a newly created [Condition].
 *
 * @param timeoutMillis the value to set timeout for the [function].
 * @param function the function to match the conditional expression.
 *
 * @throws NullPointerException if the [function] is null.
 */
fun condition(timeoutMillis: Long, function: ConditionFunction): Condition =
    Condition.of(function, timeoutMillis)

/**
 * Returns a newly created [Condition].
 *
 * @param timeout the value to set timeout for the [function].
 * @param unit the unit to set timeout for the [function].
 * @param function the function to match the conditional expression.
 *
 * @throws NullPointerException if [function] or [unit] is null.
 */
fun condition(timeout: Long, unit: TimeUnit, function: ConditionFunction): Condition =
    Condition.of(function, timeout, unit)

/**
 * Returns the [ConditionBuilder].
 */
fun conditionBuilder(): ConditionBuilder = Condition.builder()

/**
 * Returns the [ConditionComposer].
 *
 * @param operator the operator of [ComposedCondition].
 *
 * @throws NullPointerException if the [operator] is null.
 */
fun conditionComposer(operator: Operator): ConditionComposer = Condition.composer(operator)

/**
 * Returns a newly created [Condition] with true.
 */
fun `true`(): Condition = Condition.trueCondition()

/**
 * Returns a newly created [Condition] with false.
 */
fun `false`(): Condition = Condition.falseCondition()

/**
 * Returns a newly created [ConditionContext].
 */
fun conditionContext(): ConditionContext = ConditionContext.of()

/**
 * Returns a newly created [ConditionContext] by [Map].
 */
fun conditionContext(contextVariables: Map<String, Any>): ConditionContext =
    ConditionContext.of(contextVariables)

/**
 * Returns a newly created [ConditionContext] by [Pair]s.
 */
fun conditionContext(vararg contextVariables: Pair<String, Any>): ConditionContext =
    ConditionContext.of(mapOf(*contextVariables))

/**
 * Returns a newly created [ConditionContext] by [Map].
 */
fun Map<String, Any>.asConditionContext(): ConditionContext = ConditionContext.of(this)

/**
 * Returns a newly created [ConditionContext] by [Pair].
 */
fun Pair<String, Any>.asConditionContext(): ConditionContext = ConditionContext.of(mapOf(this))
