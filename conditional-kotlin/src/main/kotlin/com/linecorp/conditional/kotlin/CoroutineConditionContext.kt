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

@UnstableApi
class CoroutineConditionContext internal constructor(
    private val contextVariables: Map<String, Any> = mapOf(),
    private val logs: CopyOnWriteArrayList<CoroutineConditionMatchResult> = CopyOnWriteArrayList(),
) {
    /**
     * Returns the value corresponding to the [key] from [contextVariables].
     *
     * @param key the [key] to get value from [contextVariables].
     *
     * @return null if the value corresponding to the [key] does not exist.
     */
    fun `var`(key: String): Any? = contextVariables[key]

    /**
     * Returns a newly created [CoroutineConditionContext] from existing [CoroutineConditionContext].
     * [contextVariables] is copied from the existing [CoroutineConditionContext], but [logs] is not.
     * This is useful when invoking [CoroutineCondition.matches] methods after initializing [logs].
     */
    fun copy(): CoroutineConditionContext = coroutineConditionContext(contextVariables)

    /**
     * Returns the match logs of [CoroutineCondition].
     */
    fun logs(): List<CoroutineConditionMatchResult> = logs.toList()

    internal fun log(log: CoroutineConditionMatchResult) {
        logs += log
    }
}

/**
 * Returns a newly created [CoroutineConditionContext].
 */
fun coroutineConditionContext(): CoroutineConditionContext = CoroutineConditionContext()

/**
 * Returns a newly created [CoroutineConditionContext] by [Map].
 */
fun coroutineConditionContext(contextVariables: Map<String, Any> = mapOf()): CoroutineConditionContext =
    CoroutineConditionContext(contextVariables)

/**
 * Returns a newly created [CoroutineConditionContext] by [Pair]s.
 */
fun coroutineConditionContext(vararg contextVariables: Pair<String, Any>): CoroutineConditionContext =
    coroutineConditionContext(mapOf(*contextVariables))

/**
 * Returns a newly created [CoroutineConditionContext] by [Map].
 */
fun Map<String, Any>.asCoroutineConditionContext(): CoroutineConditionContext = coroutineConditionContext(this)

/**
 * Returns a newly created [CoroutineConditionContext] by [Pair].
 */
fun Pair<String, Any>.asCoroutineConditionContext(): CoroutineConditionContext = coroutineConditionContext(this)

/**
 * Returns the value corresponding to the [key].
 */
operator fun CoroutineConditionContext.get(key: String): Any? = this.`var`(key)
