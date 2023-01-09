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

import com.linecorp.conditional.kotlin.CoroutineConditionMatchState.*
import kotlinx.coroutines.CoroutineName
import java.time.Instant
import java.time.ZoneId
import kotlin.coroutines.CoroutineContext

@UnstableApi
class CoroutineConditionMatchResult internal constructor(
    private val thread: Thread,
    private val coroutineContext: CoroutineContext,
    private val condition: CoroutineCondition,
    private val state: CoroutineConditionMatchState,
    private val matches: Boolean?,
    private val cause: Throwable?,
    private val startTimeMillis: Long,
    private val endTimeMillis: Long,
) {
    private val durationMillis: Long

    init {
        when (state) {
            COMPLETED -> requireNotNull(matches) { "matches" }
            FAILED, CANCELLED, TIMED_OUT -> requireNotNull(cause) { "cause" }
        }
        durationMillis = endTimeMillis - startTimeMillis
    }

    override fun toString(): String = with(StringBuilder()) {
        append("CoroutineConditionMatchResult{")
            .append("condition=${condition}")
            .append(", state=${state}")
        when (state) {
            COMPLETED -> append(", matches=${matches}")
            FAILED, CANCELLED, TIMED_OUT -> append(", cause=${cause}")
        }
        append(", thread=${thread.name}")
            .append(", coroutine=${coroutineContext[CoroutineName]}")
            .append(", delay=${millisAsString(condition.delayMillis)}")
            .append(", timeout=${millisAsString(condition.timeoutMillis)}")
            .append(", startTime=${millisAsISO8601String(startTimeMillis)}")
            .append(", endTime=${millisAsISO8601String(endTimeMillis)}")
            .append(", duration=${millisAsString(durationMillis)}")
            .append('}')
        toString()
    }

    private fun millisAsString(millis: Long) = if (millis == Long.MAX_VALUE) "INF" else "${millis}ms"
    private fun millisAsISO8601String(millis: Long): String =
        with(Instant.ofEpochMilli(millis)) { atZone(ZoneId.systemDefault()).toOffsetDateTime() }.toString()
}
