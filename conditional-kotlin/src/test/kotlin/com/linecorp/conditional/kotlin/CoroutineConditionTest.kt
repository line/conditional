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

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.*
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

class CoroutineConditionTest {

    @Test
    fun create(): Unit = runBlocking(CoroutineName("coroutine")) {
        val a = coroutineCondition("a", delayMillis = 1000) { true }
        val b = coroutineCondition("b", delayMillis = 1100) { true }
        val c = coroutineCondition("c", delayMillis = 1200) { true }
        val d = coroutineCondition("d", delayMillis = 1300) { true }
        val e = coroutineCondition("e", delayMillis = 1400) { true }
        val f = coroutineCondition("f", delayMillis = 1500) { true }
        val g = coroutineCondition("g", delayMillis = 1600) { true }
        val h = coroutineCondition("h", delayMillis = 1700) { true }
        val i = coroutineCondition("i", delayMillis = 1800) { true }
        val j = coroutineCondition("j", delayMillis = 1900) { true }
        val k = coroutineCondition("k", delayMillis = 2000) { true }
        val l = coroutineCondition("l", delayMillis = 2100) { true }
        val m = coroutineCondition("m", delayMillis = 2200) { true }
        val n = coroutineCondition("n", delayMillis = 2300) { true }
        val o = coroutineCondition("o", delayMillis = 2400) { true }
        val p = coroutineCondition("p", delayMillis = 2500) { true }
        val q = coroutineCondition("q", delayMillis = 2600) { true }
        val r = coroutineCondition("r", delayMillis = 2700) { true }
        val s = coroutineCondition("s", delayMillis = 2800) { true }
        val t = coroutineCondition("t", delayMillis = 2900) { true }
        val u = coroutineCondition("u", delayMillis = 3000) { true }
        val v = coroutineCondition("v", delayMillis = 3100) { true }
        val w = coroutineCondition("w", delayMillis = 3200) { true }
        val x = coroutineCondition("x", delayMillis = 3300) { true }
        val y = coroutineCondition("y", delayMillis = 3400) { true }
        val z = coroutineCondition("z", delayMillis = 3500) { true }
        val condition = a and (b and (c and (d and (e and (f and (g
                and (h and (i and (j and (k and (l and (m and (n
                and (o and (p and (q and (r and (s and (t and (u
                and (v and (w and (x and (y and z))))))))))))))))))))))))
        val ctx = coroutineConditionContext()
        await().atLeast(3500, TimeUnit.MILLISECONDS)
            .atMost(4000, TimeUnit.MILLISECONDS)
            .until {
                runBlocking { condition.matches(ctx) }.also { assertThat(it).isTrue }
                true
            }
    }

    @Test
    fun update_alias() {
        val condition = coroutineCondition { true }
        assertThat(condition.alias).isEqualTo(CoroutineCondition.DEFAULT_ALIAS)

        val updated = condition.alias { "AliasedCoroutineCondition" }
        assertThat(updated.alias).isEqualTo("AliasedCoroutineCondition")
        assertThat(condition.alias).isEqualTo(CoroutineCondition.DEFAULT_ALIAS)
    }

    @Test
    fun update_delayMillis() {
        val condition = coroutineCondition { true }
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)

        val updated = condition.delayMillis { 1000 }
        assertThat(updated.delayMillis).isEqualTo(1000)
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)
    }

    @Test
    fun update_delay() {
        val condition = coroutineCondition { true }
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)

        val updated = condition.delay { Duration.ofMillis(1000) }
        assertThat(updated.delayMillis).isEqualTo(1000)
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)
    }

    @Test
    fun update_timeoutMillis() {
        val condition = coroutineCondition { true }
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)

        val updated = condition.timeoutMillis { 1000 }
        assertThat(updated.timeoutMillis).isEqualTo(1000)
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)
    }

    @Test
    fun update_timeout() {
        val condition = coroutineCondition { true }
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)

        val updated = condition.timeout { Duration.ofMillis(1000) }
        assertThat(updated.timeoutMillis).isEqualTo(1000)
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)
    }

    @Test
    fun delay() {
        val condition = coroutineCondition(delayMillis = 1000) { true }
        val ctx = coroutineConditionContext()
        await().atLeast(1000, TimeUnit.MILLISECONDS)
            .atMost(1500, TimeUnit.MILLISECONDS)
            .until {
                runBlocking { condition.matches(ctx) }.also { assertThat(it).isTrue }
                true
            }
    }

    @Test
    fun timeout() {
        val condition = coroutineCondition(timeoutMillis = 1000L) {
            delay(500L)
            true
        }
        val ctx = coroutineConditionContext()
        await().atLeast(500, TimeUnit.MILLISECONDS)
            .atMost(900, TimeUnit.MILLISECONDS)
            .until {
                assertThatCode {
                    runBlocking { condition.matches(ctx) }.also { assertThat(it).isTrue }
                }.doesNotThrowAnyException()
                true
            }
    }

    @Test
    fun timeout_raised(): Unit = runBlocking {
        val condition = coroutineCondition(timeoutMillis = 1000L) {
            delay(2000L)
            true
        }
        val ctx = coroutineConditionContext()
        await().atLeast(1000, TimeUnit.MILLISECONDS)
            .atMost(1500, TimeUnit.MILLISECONDS)
            .until {
                assertThatThrownBy {
                    runBlocking {
                        condition.matches(ctx)
                        fail("If the TimeoutCancellationException is raised, this code should not run.")
                    }
                }.isExactlyInstanceOf(TimeoutCancellationException::class.java)
                true
            }
    }

    @Test
    fun negate_matches() {
        val condition = coroutineCondition("CoroutineCondition") { true }
        val negate = condition.negate();
        runBlocking { condition.matches(coroutineConditionContext()) }.also { assertThat(it).isTrue }
        runBlocking { negate.matches(coroutineConditionContext()) }.also { assertThat(it).isFalse }
    }

    @Test
    fun not_matches() {
        val condition = coroutineCondition("CoroutineCondition") { true }
        val not = !condition
        runBlocking { condition.matches(coroutineConditionContext()) }.also { assertThat(it).isTrue }
        runBlocking { not.matches(coroutineConditionContext()) }.also { assertThat(it).isFalse }
    }

    @Test
    fun testToString() {
        object : CoroutineCondition() {
            override suspend fun match(ctx: CoroutineConditionContext): Boolean = true
        }.toString().also { assertThat(it).isEqualTo("Undefined") }
        coroutineCondition { true }.toString().also { assertThat(it).isEqualTo("Undefined") }
        coroutineCondition { false }.toString().also { assertThat(it).isEqualTo("Undefined") }
        coroutineCondition(alias = "TrueCondition") { true }.toString()
            .also { assertThat(it).isEqualTo("TrueCondition") }
        coroutineCondition(alias = "FalseCondition") { false }.toString()
            .also { assertThat(it).isEqualTo("FalseCondition") }
    }
}
