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

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

@UnstableApi
class ComposedCoroutineCondition internal constructor(
    private val operator: CoroutineConditionOperator,
    private val conditions: List<CoroutineCondition>,
) : CoroutineCondition() {
    init {
        if (conditions.isEmpty()) {
            throw IllegalArgumentException("conditions is empty (expected not empty)")
        }
    }

    override suspend fun match(ctx: CoroutineConditionContext): Boolean = coroutineScope {
        val dl = conditions.map { async { it.matches(ctx) } }
        val it = dl.iterator()
        var value = it.next().await()
        if (shortCircuit(operator, value)) return@coroutineScope cancelWith(dl) { value }
        while (it.hasNext()) {
            val next = it.next().await()
            value = when (operator) {
                CoroutineConditionOperator.AND -> value && next
                CoroutineConditionOperator.OR -> value || next
            }
            if (shortCircuit(operator, value)) return@coroutineScope cancelWith(dl) { value }
        }
        value
    }

    private suspend fun <T> cancelWith(dl: List<Deferred<Boolean>>, value: suspend () -> T): T {
        dl.forEach { it.cancel() }
        return value()
    }

    private fun shortCircuit(operator: CoroutineConditionOperator, value: Boolean): Boolean = when (operator) {
        CoroutineConditionOperator.AND -> !value
        CoroutineConditionOperator.OR -> value
    }

    override fun toString(): String {
        if (!alias.isNullOrBlank()) return alias
        assert(conditions.isNotEmpty())
        if (conditions.size == 1) return conditions[0].toString()
        return StringJoiner(
            when (operator) {
                CoroutineConditionOperator.AND -> DELIMITER_AND
                CoroutineConditionOperator.OR -> DELIMITER_OR
            }, PREFIX, SUFFIX
        ).also { for (condition in conditions) it.add(condition.toString()) }.toString()
    }

    private companion object {
        @JvmStatic
        private val PREFIX = "("

        @JvmStatic
        private val SUFFIX = ")"

        @JvmStatic
        private val DELIMITER_AND = " and "

        @JvmStatic
        private val DELIMITER_OR = " or "
    }
}
