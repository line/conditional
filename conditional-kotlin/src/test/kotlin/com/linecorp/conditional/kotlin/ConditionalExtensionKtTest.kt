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

import com.linecorp.conditional.Operator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class ConditionalExtensionKtTest {

    @Test
    fun condition() {
        val condition = condition { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun condition_with_timeoutMillis() {
        val condition = condition(1000) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun condition_with_timeout() {
        val condition = condition(1000, TimeUnit.MILLISECONDS) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asCondition() {
        val condition = true.asCondition()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition() {
        val condition = asyncCondition { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition_with_executor() {
        val executor = ForkJoinPool.commonPool()
        val condition = asyncCondition(executor) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition_with_timeoutMillis() {
        val condition = asyncCondition(1000) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition_with_timeoutMillis_and_executor() {
        val executor = ForkJoinPool.commonPool()
        val condition = asyncCondition(1000, executor) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition_with_timeout() {
        val condition = asyncCondition(1000, TimeUnit.MILLISECONDS) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asyncCondition_with_timeout_and_executor() {
        val executor = ForkJoinPool.commonPool()
        val condition = asyncCondition(1000, TimeUnit.MILLISECONDS, executor) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asAsyncCondition() {
        val condition = true.asAsyncCondition()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asAsyncCondition_with_executor() {
        val executor = ForkJoinPool.commonPool()
        val condition = true.asAsyncCondition(executor)
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun delayedCondition_with_delayMillis() {
        val condition = delayedCondition(1000) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun delayedCondition_with_delay() {
        val condition = delayedCondition(1000, TimeUnit.MILLISECONDS) { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asDelayedCondition_with_delayMillis() {
        val condition = true.asDelayedCondition(1000)
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun asDelayedCondition_with_delay() {
        val condition = true.asDelayedCondition(1000, TimeUnit.MILLISECONDS)
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun conditionComposer() {
        val composer = conditionComposer(Operator.AND)
        val condition = composer.with(`true`(), `true`()).compose()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun completed() {
        val condition = completed(true)
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun trueCondition() {
        val condition = `true`()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun falseCondition() {
        val condition = `false`()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isFalse
    }

    @Test
    fun failed_with_supplier() {
        val condition = failed { RuntimeException() }
        val ctx = conditionContext()
        assertThatThrownBy { condition.matches(ctx) }
            .isExactlyInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun failed_with_contextAwareSupplier() {
        val condition = failed { _ -> RuntimeException() }
        val ctx = conditionContext()
        assertThatThrownBy { condition.matches(ctx) }
            .isExactlyInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun asConditionContext() {
        val condition = condition { ctx ->
            ctx.`var`("a") as Boolean
        }
        val ctx = mapOf("a" to true).asConditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }
}
