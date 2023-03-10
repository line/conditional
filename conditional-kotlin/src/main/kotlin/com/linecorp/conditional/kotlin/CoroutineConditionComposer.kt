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

import java.util.concurrent.CopyOnWriteArrayList

class CoroutineConditionComposer internal constructor(
    private val operator: CoroutineConditionOperator,
    private val conditions: CopyOnWriteArrayList<CoroutineCondition> = CopyOnWriteArrayList(),
) {
    /**
     * Returns the [CoroutineConditionComposer] composed by [conditions].
     *
     * @param conditions the [conditions] to compose.
     */
    fun with(vararg conditions: CoroutineCondition) = with(conditions.toList())

    /**
     * Returns the [CoroutineConditionComposer] composed by [conditions].
     *
     * @param conditions the [conditions] to compose.
     */
    fun with(conditions: List<CoroutineCondition>) = also {
        if (conditions.isNotEmpty()) this.conditions += conditions
    }

    /**
     * Returns a newly created [ComposedCoroutineCondition] by [CoroutineConditionComposer].
     */
    fun compose() = ComposedCoroutineCondition(operator, conditions)
}

/**
 * Returns a newly created [CoroutineConditionComposer].
 */
fun coroutineConditionComposer(operator: CoroutineConditionOperator) = CoroutineConditionComposer(operator)
