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
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CoroutineConditionTest {

    @Test
    fun create(): Unit = runBlocking(CoroutineName("coroutine")) {
        val a = coroutineCondition("a", delayMillis = 1000L) { true }
        val b = coroutineCondition("b", delayMillis = 1100L) { true }
        val c = coroutineCondition("c", delayMillis = 1200L) { true }
        val d = coroutineCondition("d", delayMillis = 1300L) { true }
        val e = coroutineCondition("e", delayMillis = 1400L) { true }
        val f = coroutineCondition("f", delayMillis = 1500L) { true }
        val g = coroutineCondition("g", delayMillis = 1600L) { true }
        val h = coroutineCondition("h", delayMillis = 1700L) { true }
        val i = coroutineCondition("i", delayMillis = 1800L) { true }
        val j = coroutineCondition("j", delayMillis = 1900L) { true }
        val k = coroutineCondition("k", delayMillis = 2000L) { true }
        val l = coroutineCondition("l", delayMillis = 2100L) { true }
        val m = coroutineCondition("m", delayMillis = 2200L) { true }
        val n = coroutineCondition("n", delayMillis = 2300L) { true }
        val o = coroutineCondition("o", delayMillis = 2400L) { true }
        val p = coroutineCondition("p", delayMillis = 2500L) { true }
        val q = coroutineCondition("q", delayMillis = 2600L) { true }
        val r = coroutineCondition("r", delayMillis = 2700L) { true }
        val s = coroutineCondition("s", delayMillis = 2800L) { true }
        val t = coroutineCondition("t", delayMillis = 2900L) { true }
        val u = coroutineCondition("u", delayMillis = 3000L) { true }
        val v = coroutineCondition("v", delayMillis = 3100L) { true }
        val w = coroutineCondition("w", delayMillis = 3200L) { true }
        val x = coroutineCondition("x", delayMillis = 3300L) { true }
        val y = coroutineCondition("y", delayMillis = 3400L) { true }
        val z = coroutineCondition("z", delayMillis = 3500L) { true }
        val condition = a and (b and (c and (d and (e and (f and (g
                and (h and (i and (j and (k and (l and (m and (n
                and (o and (p and (q and (r and (s and (t and (u
                and (v and (w and (x and (y and z))))))))))))))))))))))))
        val ctx = coroutineConditionContext()
        await().atLeast(3500L, TimeUnit.MILLISECONDS)
            .atMost(4000L, TimeUnit.MILLISECONDS)
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

        val updated = condition.delayMillis { 1000L }
        assertThat(updated.delayMillis).isEqualTo(1000L)
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)
    }

    @Test
    fun update_delay() {
        val condition = coroutineCondition { true }
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)

        val updated = condition.delay { 1000L.toDuration(DurationUnit.MILLISECONDS) }
        assertThat(updated.delayMillis).isEqualTo(1000L)
        assertThat(condition.delayMillis).isEqualTo(CoroutineCondition.DEFAULT_DELAY_MILLIS)
    }

    @Test
    fun update_timeoutMillis() {
        val condition = coroutineCondition { true }
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)

        val updated = condition.timeoutMillis { 1000L }
        assertThat(updated.timeoutMillis).isEqualTo(1000L)
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)
    }

    @Test
    fun update_timeout() {
        val condition = coroutineCondition { true }
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)

        val updated = condition.timeout { 1000L.toDuration(DurationUnit.MILLISECONDS) }
        assertThat(updated.timeoutMillis).isEqualTo(1000L)
        assertThat(condition.timeoutMillis).isEqualTo(CoroutineCondition.DEFAULT_TIMEOUT_MILLIS)
    }

    @Test
    fun delay() {
        val condition = coroutineCondition(delayMillis = 1000L) { true }
        val ctx = coroutineConditionContext()
        await().atLeast(1000L, TimeUnit.MILLISECONDS)
            .atMost(1500L, TimeUnit.MILLISECONDS)
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
        await().atLeast(500L, TimeUnit.MILLISECONDS)
            .atMost(900L, TimeUnit.MILLISECONDS)
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
        await().atLeast(1000L, TimeUnit.MILLISECONDS)
            .atMost(1500L, TimeUnit.MILLISECONDS)
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
