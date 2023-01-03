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

class ConditionalExtensionKtTest {

    @Test
    fun testCondition() {
        val condition = condition { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testCondition_with_alias() {
        val condition = condition(alias = "condition") { true }
        val ctx = conditionContext()
        assertThat(condition.alias()).isEqualTo("condition")
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testCondition_with_executor() {
        val executor = ForkJoinPool.commonPool()
        val condition = condition(executor = executor) { true }
        val ctx = conditionContext()
        assertThat(condition.executor()).isEqualTo(executor)
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testCondition_with_delayMillis() {
        val condition = condition(delayMillis = 1000) { true }
        val ctx = conditionContext()
        assertThat(condition.delayMillis()).isEqualTo(1000)
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testCondition_with_timeoutMillis() {
        val condition = condition(timeoutMillis = 1000) { true }
        val ctx = conditionContext()
        assertThat(condition.timeoutMillis()).isEqualTo(1000)
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testCondition_with_cancellable() {
        val condition = condition(cancellable = false) { true }
        val ctx = conditionContext()
        assertThat(condition.cancellable()).isFalse
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testConditionBuilder() {
        val condition = conditionBuilder().build { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testConditionComposer() {
        val composer = conditionComposer(Operator.AND)
        val condition = composer.with(`true`(), `true`()).compose()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testTrueCondition() {
        val condition = `true`()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testFalseCondition() {
        val condition = `false`()
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isFalse
    }

    @Test
    fun testConditionContext() {
        val condition = condition { true }
        val ctx = conditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testConditionContext_by_map() {
        val condition = condition { ctx ->
            ctx.`var`("a") as Boolean
        }
        val ctx = conditionContext(mapOf("a" to true))
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testConditionContext_by_pairs() {
        val condition = condition { ctx ->
            val a = ctx.`var`("a") as Boolean
            val b = ctx.`var`("b") as Boolean
            a and b
        }
        val ctx = conditionContext("a" to true, "b" to true)
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testAsConditionContext_by_map() {
        val condition = condition { ctx ->
            ctx.`var`("a") as Boolean
        }
        val ctx = mapOf("a" to true).asConditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testAsConditionContext_by_pair() {
        val condition = condition { ctx ->
            ctx.`var`("a") as Boolean
        }
        val ctx = ("a" to true).asConditionContext()
        assertThat(condition.matches(ctx)).isTrue
    }

    @Test
    fun testVar() {
        val ctx = conditionContext("a" to true)
        assertThat(ctx.`var`("a")).isEqualTo(true)
        assertThat(ctx.`var`("b")).isNull()
        assertThat(ctx["a"]).isEqualTo(true)
        assertThat(ctx["b"]).isNull()
    }

    @Test
    fun testVar_with_cast_operator() {
        val ctx = conditionContext("a" to true)
        assertThat(ctx.`var`("a") as? Boolean).isTrue
        assertThat(ctx.`var`("a") as Boolean).isTrue
        assertThat(ctx.`var`("a") as? Long).isNull()
        assertThatThrownBy { ctx.`var`("a") as Long }
            .isExactlyInstanceOf(ClassCastException::class.java)
    }

    @Test
    fun testMustVar() {
        val ctx = conditionContext("a" to true)
        assertThat(ctx.mustVar("a")).isEqualTo(true)
        assertThatThrownBy { ctx.mustVar("b") }
            .isExactlyInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun testMustVar_with_cast_operator() {
        val ctx = conditionContext("a" to true)
        assertThat(ctx.mustVar("a") as? Boolean).isTrue
        assertThat(ctx.mustVar("a") as Boolean).isTrue
        assertThat(ctx.mustVar("a") as? Long).isNull()
        assertThatThrownBy { ctx.mustVar("a") as Long }
            .isExactlyInstanceOf(ClassCastException::class.java)
    }
}
