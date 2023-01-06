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

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ComposableCoroutineConditionTest {

    @Test
    fun compose_matches() {
        val condition = object : ComposableCoroutineCondition() {
            override suspend fun compose(): CoroutineCondition =
                coroutineCondition("a") { true } and coroutineCondition("b") { true }
        }
        val ctx = coroutineConditionContext()
        runBlocking { condition.matches(ctx) }.also { assertThat(it).isTrue }
    }
}