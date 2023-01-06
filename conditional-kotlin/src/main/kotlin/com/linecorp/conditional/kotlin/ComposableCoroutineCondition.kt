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

@UnstableApi
abstract class ComposableCoroutineCondition(
    alias: String? = DEFAULT_ALIAS,
    delayMillis: Long = DEFAULT_DELAY_MILLIS,
    timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
) : CoroutineCondition(alias, delayMillis, timeoutMillis) {
    final override suspend fun match(ctx: CoroutineConditionContext): Boolean = compose().matches(ctx)
    protected abstract suspend fun compose(): CoroutineCondition
}