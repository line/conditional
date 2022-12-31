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
import java.util.concurrent.TimeUnit

infix fun Condition.and(condition: Condition): ComposedCondition = and(condition)
infix fun Condition.or(condition: Condition): ComposedCondition = or(condition)
operator fun Condition.not(): Condition = Condition.not(this)

fun condition(function: ConditionFunction): Condition = Condition.of(function)
fun condition(timeoutMillis: Long, function: ConditionFunction): Condition =
    Condition.of(function, timeoutMillis)

fun condition(timeout: Long, unit: TimeUnit, function: ConditionFunction): Condition =
    Condition.of(function, timeout, unit)

fun Boolean.asCondition(): Condition = condition { this }
fun asyncCondition(function: ConditionFunction): Condition = Condition.async(function)
fun asyncCondition(executor: Executor, function: ConditionFunction): Condition =
    Condition.async(function, executor)

fun asyncCondition(timeoutMillis: Long, function: ConditionFunction): Condition =
    Condition.async(function, timeoutMillis)

fun asyncCondition(timeoutMillis: Long, executor: Executor, function: ConditionFunction): Condition =
    Condition.async(function, timeoutMillis, executor)

fun asyncCondition(timeout: Long, unit: TimeUnit, function: ConditionFunction): Condition =
    Condition.async(function, timeout, unit)

fun asyncCondition(timeout: Long, unit: TimeUnit, executor: Executor, function: ConditionFunction): Condition =
    Condition.async(function, timeout, unit, executor)

fun Boolean.asAsyncCondition(): Condition = asyncCondition { this }
fun Boolean.asAsyncCondition(executor: Executor): Condition = asyncCondition(executor) { this }
fun delayedCondition(delayMillis: Long, function: ConditionFunction): Condition =
    Condition.delayed(function, delayMillis)

fun delayedCondition(delay: Long, unit: TimeUnit, function: ConditionFunction): Condition =
    Condition.delayed(function, delay, unit)

fun Boolean.asDelayedCondition(delayMillis: Long): Condition = delayedCondition(delayMillis) { this }
fun Boolean.asDelayedCondition(delay: Long, unit: TimeUnit): Condition = delayedCondition(delay, unit) { this }

fun conditionComposer(operator: Operator): ConditionComposer = Condition.composer(operator)

fun completed(value: Boolean): Condition = Condition.completed(value)
fun `true`(): Condition = Condition.trueCondition()
fun `false`(): Condition = Condition.falseCondition()
fun <T : Throwable> failed(exceptionSupplier: () -> T): Condition = Condition.failed(exceptionSupplier)
fun <T : Throwable> failed(exceptionSupplier: ConditionContextAwareSupplier<T>): Condition =
    Condition.failed(exceptionSupplier)

fun conditionContext(): ConditionContext = ConditionContext.of()
fun conditionContext(contextVariables: Map<String, Any>): ConditionContext =
    ConditionContext.of(contextVariables)

fun Map<String, Any>.asConditionContext(): ConditionContext = conditionContext(this)
