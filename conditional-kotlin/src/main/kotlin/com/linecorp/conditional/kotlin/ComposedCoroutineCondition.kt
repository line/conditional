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

import com.linecorp.conditional.kotlin.CoroutineConditionOperator.AND
import com.linecorp.conditional.kotlin.CoroutineConditionOperator.OR
import kotlinx.coroutines.*
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
        val ds = mutableListOf<Deferred<Boolean>>()
        conditions.forEach {
            val d = async { it.matches(ctx) }
            if (completed(d)) return@coroutineScope cancelWith(ds, d::await)
            ds += d
        }
        try {
            if (completedAsync(ds).await()) return@coroutineScope cancelWith(ds) {
                when (operator) {
                    AND -> false
                    OR -> true
                }
            }
        } catch (e: Exception) {
            cancel(ds)
            throw e
        }
        val it = ds.iterator()
        var value = it.next().await()
        while (it.hasNext()) {
            val next = it.next().await()
            value = when (operator) {
                AND -> value && next
                OR -> value || next
            }
        }
        value
    }

    private suspend fun completed(d: Deferred<Boolean>): Boolean =
        d.isCompleted && shortCircuit(operator, d.await())

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun completedAsync(ds: List<Deferred<Boolean>>): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        for (d in ds) {
            if (d.isCompleted) {
                break
            }
            d.invokeOnCompletion { e ->
                if (e != null) {
                    completeExceptionally(deferred, e)
                    cancel(ds)
                } else if (shortCircuit(operator, d.getCompleted())) {
                    complete(deferred, true)
                    cancel(ds)
                }
            }
        }
        if (!deferred.isCompleted) {
            coroutineScope {
                launch {
                    ds.awaitAll()
                    complete(deferred, false)
                }
            }
        }
        return deferred
    }

    private fun shortCircuit(operator: CoroutineConditionOperator, value: Boolean): Boolean = when (operator) {
        AND -> !value
        OR -> value
    }

    private fun complete(cd: CompletableDeferred<Boolean>, value: Boolean) {
        if (!cd.isCompleted) cd.complete(value)
    }

    private fun completeExceptionally(cd: CompletableDeferred<Boolean>, e: Throwable) {
        if (!cd.isCompleted) cd.completeExceptionally(e)
    }

    private fun cancel(ds: List<Deferred<Boolean>>) {
        for (d in ds) if (!d.isCompleted) d.cancel()
    }

    private suspend fun <T> cancelWith(ds: List<Deferred<Boolean>>, value: suspend () -> T): T {
        cancel(ds)
        return value()
    }

    override fun toString(): String {
        if (!alias.isNullOrBlank()) return alias
        assert(conditions.isNotEmpty())
        if (conditions.size == 1) return conditions[0].toString()
        return StringJoiner(
            when (operator) {
                AND -> DELIMITER_AND
                OR -> DELIMITER_OR
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
