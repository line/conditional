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

import java.time.Duration
import java.util.concurrent.TimeUnit

@UnstableApi
class CoroutineConditionBuilder internal constructor(
    @Volatile private var alias: String? = CoroutineCondition.DEFAULT_ALIAS,
    @Volatile private var delayMillis: Long = CoroutineCondition.DEFAULT_DELAY_MILLIS,
    @Volatile private var timeoutMillis: Long = CoroutineCondition.DEFAULT_TIMEOUT_MILLIS,
) {
    /**
     * Returns the [CoroutineConditionBuilder] with [alias] set.
     */
    fun alias(alias: String?): CoroutineConditionBuilder = also { this.alias = alias }

    /**
     * Returns the [CoroutineConditionBuilder] with [delayMillis] is set.
     */
    fun delay(delayMillis: Long): CoroutineConditionBuilder = also { this.delayMillis = delayMillis }

    /**
     * Returns the [CoroutineConditionBuilder] with [delayMillis] is set.
     */
    fun delay(delay: Long, unit: TimeUnit): CoroutineConditionBuilder =
        also { delayMillis = unit.toMillis(delay) }

    /**
     * Returns the [CoroutineConditionBuilder] with [delayMillis] is set.
     */
    fun delay(delay: Duration): CoroutineConditionBuilder = also { delayMillis = delay.toMillis() }

    /**
     * Returns the [CoroutineConditionBuilder] with [timeoutMillis] is set.
     */
    fun timeout(timeoutMillis: Long): CoroutineConditionBuilder = also { this.timeoutMillis = timeoutMillis }

    /**
     * Returns the [CoroutineConditionBuilder] with [timeoutMillis] is set.
     */
    fun timeout(timeout: Long, unit: TimeUnit): CoroutineConditionBuilder =
        also { timeoutMillis = unit.toMillis(timeout) }

    /**
     * Returns the [CoroutineConditionBuilder] with [timeoutMillis] is set.
     */
    fun timeout(timeout: Duration): CoroutineConditionBuilder = also { timeoutMillis = timeout.toMillis() }

    /**
     * Returns a newly created [CoroutineCondition] by [CoroutineConditionBuilder].
     */
    fun build(function: CoroutineConditionFunction): CoroutineCondition =
        object : CoroutineCondition(alias, delayMillis, timeoutMillis) {
            override suspend fun match(ctx: CoroutineConditionContext): Boolean = function(ctx)
        }
}

/**
 * Returns a newly created [CoroutineConditionBuilder].
 */
fun coroutineConditionBuilder() = CoroutineConditionBuilder()
